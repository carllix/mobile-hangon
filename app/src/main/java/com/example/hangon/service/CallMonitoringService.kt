package com.example.hangon.service

import android.Manifest
import android.annotation.SuppressLint
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.content.pm.PackageManager
import android.content.pm.ServiceInfo
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaRecorder
import android.net.Uri
import android.os.Binder
import android.os.IBinder
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.content.ContextCompat
import com.example.hangon.BuildConfig
import com.example.hangon.R
import com.example.hangon.data.remote.ws.CallServerEvent
import com.example.hangon.data.remote.ws.CodewordSubmitMessage
import com.example.hangon.data.remote.ws.EndSessionMessage
import com.example.hangon.data.remote.ws.StartSessionMessage
import com.example.hangon.data.remote.ws.UserDecisionMessage
import com.example.hangon.data.remote.ws.VoiceCheckResponseMessage
import com.example.hangon.data.remote.ws.parseCallServerEvent
import com.example.hangon.data.util.httpToWs
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import okio.ByteString.Companion.toByteString
import java.util.concurrent.TimeUnit

class CallMonitoringService : Service() {

    inner class LocalBinder : Binder() {
        fun getService(): CallMonitoringService = this@CallMonitoringService
    }

    private val binder = LocalBinder()
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(serviceJob + Dispatchers.IO)

    private val _events = MutableSharedFlow<CallServerEvent>(extraBufferCapacity = 16)
    val events: SharedFlow<CallServerEvent> = _events.asSharedFlow()

    private val json = Json { encodeDefaults = true }
    private val wsClient = OkHttpClient.Builder()
        .readTimeout(0, TimeUnit.MILLISECONDS)
        .build()

    private var webSocket: WebSocket? = null
    private var audioJob: Job? = null
    private var audioRecord: AudioRecord? = null

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
    }

    fun startMonitoring(callId: String, phoneNumber: String, idToken: String) {
        ServiceCompat.startForeground(
            this,
            NOTIFICATION_ID,
            buildNotification(),
            ServiceInfo.FOREGROUND_SERVICE_TYPE_MICROPHONE
        )
        connectWebSocket(callId, phoneNumber, idToken)
    }

    fun sendUserDecision(choice: String) {
        send(UserDecisionMessage(choice = choice))
    }

    fun sendVoiceCheckResponse(recognized: Boolean, familyId: String?) {
        send(VoiceCheckResponseMessage(recognized = recognized, familyId = familyId))
    }

    fun sendCodewordSubmit(value: String) {
        send(CodewordSubmitMessage(value = value))
    }

    fun endSession() {
        stopAudioCapture()
        send(EndSessionMessage())
    }

    fun stopMonitoring() {
        stopAudioCapture()
        webSocket?.close(1000, "client_stop")
        webSocket = null
        ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        stopAudioCapture()
        webSocket?.close(1000, "service_destroyed")
        serviceJob.cancel()
        super.onDestroy()
    }

    private inline fun <reified T> send(message: T) {
        webSocket?.send(json.encodeToString(message))
    }

    private fun connectWebSocket(callId: String, phoneNumber: String, idToken: String) {
        val wsUrl = httpToWs(BuildConfig.BASE_URL) + "ws/calls/$callId?token=${Uri.encode(idToken)}"
        val request = Request.Builder().url(wsUrl).build()
        webSocket = wsClient.newWebSocket(request, object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                webSocket.send(json.encodeToString(StartSessionMessage(phoneNumber = phoneNumber)))
                startAudioCapture(webSocket)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseCallServerEvent(text)?.let { event -> serviceScope.launch { _events.emit(event) } }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                Log.w(TAG, "Call WebSocket failure", t)
                stopAudioCapture()
                serviceScope.launch {
                    _events.emit(CallServerEvent.Error(t.message ?: "Koneksi ke server terputus"))
                }
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                stopAudioCapture()
            }
        })
    }

    @SuppressLint("MissingPermission")
    private fun startAudioCapture(webSocket: WebSocket) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            serviceScope.launch { _events.emit(CallServerEvent.Error("Izin mikrofon tidak diberikan")) }
            return
        }

        val minBufferSize = AudioRecord.getMinBufferSize(
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT
        )
        if (minBufferSize <= 0) return

        val record = AudioRecord(
            MediaRecorder.AudioSource.VOICE_COMMUNICATION,
            SAMPLE_RATE_HZ,
            AudioFormat.CHANNEL_IN_MONO,
            AudioFormat.ENCODING_PCM_16BIT,
            minBufferSize * 2
        )
        if (record.state != AudioRecord.STATE_INITIALIZED) {
            record.release()
            return
        }

        audioRecord = record
        record.startRecording()

        audioJob = serviceScope.launch {
            val buffer = ByteArray(minBufferSize)
            while (isActive) {
                val read = record.read(buffer, 0, buffer.size)
                if (read > 0) {
                    webSocket.send(buffer.copyOf(read).toByteString())
                }
            }
        }
    }

    private fun stopAudioCapture() {
        audioJob?.cancel()
        audioJob = null
        audioRecord?.let {
            try {
                it.stop()
            } catch (_: IllegalStateException) {
            }
            it.release()
        }
        audioRecord = null
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Pemantauan Panggilan",
            NotificationManager.IMPORTANCE_LOW
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    private fun buildNotification(): Notification =
        NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("HangOn sedang memantau panggilan")
            .setContentText("Audio dianalisis untuk mendeteksi indikasi penipuan.")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setOngoing(true)
            .build()

    companion object {
        private const val TAG = "CallMonitoringService"
        private const val CHANNEL_ID = "call_monitoring"
        private const val NOTIFICATION_ID = 1001
        private const val SAMPLE_RATE_HZ = 16000
    }
}
