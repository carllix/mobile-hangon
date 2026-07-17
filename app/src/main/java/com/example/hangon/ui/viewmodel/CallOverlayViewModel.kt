package com.example.hangon.ui.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.hangon.data.remote.ws.CallServerEvent
import com.example.hangon.data.remote.ws.UserDecisionChoice
import com.example.hangon.data.repository.ApiResult
import com.example.hangon.data.repository.AuthRepository
import com.example.hangon.data.repository.FamilyRepository
import com.example.hangon.data.repository.FirebaseAuthRepository
import com.example.hangon.data.repository.RetrofitFamilyRepository
import com.example.hangon.service.CallMonitoringService
import java.util.UUID
import kotlin.math.roundToInt
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private const val CODEWORD_MAX_TRIES = 2
private const val CODEWORD_VERIFIED_DISPLAY_MS = 2500L
private const val RESUMED_BANNER_DISPLAY_MS = 2500L
private const val SESSION_SUMMARY_DISPLAY_MS = 3000L

enum class CallStage {
    RINGING_CONSENT,
    IN_CALL_MONITORING,
    SUSPICIOUS_FLAGGED,
    AWAITING_VOICE_CHECK,
    CODEWORD_PROMPT,
    CODEWORD_INPUT,
    CODEWORD_VERIFIED,
    AWAITING_RISK_DECISION,
    SESSION_SUMMARY,
    ENDED
}

data class CallOverlayUiState(
    val callerNumber: String = "+1 2345 678 90",
    val stage: CallStage = CallStage.RINGING_CONSENT,
    val callElapsedSeconds: Int = 0,
    val confidence: Int = 0,
    val reasoning: String = "",
    val codewordInput: String = "",
    val triesLeft: Int = CODEWORD_MAX_TRIES,
    val codewordError: String? = null,
    val isVerifyingCodeword: Boolean = false,
    val resumedMonitoringBanner: Boolean = false,
    val verificationStatus: String? = null,
    val connectionError: String? = null
)

class CallOverlayViewModel(
    application: Application,
    callerNumber: String,
    private val familyRepository: FamilyRepository = RetrofitFamilyRepository(),
    private val authRepository: AuthRepository = FirebaseAuthRepository()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(CallOverlayUiState(callerNumber = callerNumber))
    val uiState: StateFlow<CallOverlayUiState> = _uiState.asStateFlow()

    private val callId = UUID.randomUUID().toString()
    private var familyId: String? = null
    private var callTimerJob: Job? = null
    private var eventsJob: Job? = null

    @SuppressLint("StaticFieldLeak")
    private var monitoringService: CallMonitoringService? = null
    private var bound = false

    private val connection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, binder: IBinder?) {
            val service = (binder as CallMonitoringService.LocalBinder).getService()
            monitoringService = service
            eventsJob = service.events.onEach(::handleServerEvent).launchIn(viewModelScope)
            viewModelScope.launch {
                val idToken = authRepository.getIdToken()
                if (idToken == null) {
                    _uiState.update { it.copy(connectionError = "Invalid session, please sign in again.") }
                    return@launch
                }
                service.startMonitoring(callId, _uiState.value.callerNumber, idToken)
            }
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            monitoringService = null
        }
    }

    init {
        viewModelScope.launch {
            val result = familyRepository.listMyFamilies()
            familyId = (result as? ApiResult.Success)?.data?.firstOrNull()?.id
        }
    }

    fun onAcceptWithHangOn() {
        _uiState.update { it.copy(stage = CallStage.IN_CALL_MONITORING) }
        startCallTimer()
        val context = getApplication<Application>()
        val intent = Intent(context, CallMonitoringService::class.java)
        context.startService(intent)
        context.bindService(intent, connection, Context.BIND_AUTO_CREATE)
        bound = true
    }

    fun onSkip() {
        _uiState.update { it.copy(stage = CallStage.ENDED) }
    }

    fun onContinueAfterSuspicious() {
        monitoringService?.sendUserDecision(UserDecisionChoice.LANJUTKAN)
    }

    fun onEndCallFromSuspicious() {
        monitoringService?.sendUserDecision(UserDecisionChoice.TUTUP)
        endCall()
    }

    fun onVoiceCheckResponse(recognized: Boolean) {
        monitoringService?.sendVoiceCheckResponse(recognized, if (recognized) familyId else null)
    }

    fun onRequestCodewordInput() {
        _uiState.update { it.copy(stage = CallStage.CODEWORD_INPUT) }
    }

    fun onCodewordInputChange(value: String) {
        _uiState.update { it.copy(codewordInput = value, codewordError = null) }
    }

    fun onVerifyCodeword() {
        val input = _uiState.value.codewordInput
        if (input.isBlank()) return
        _uiState.update { it.copy(isVerifyingCodeword = true) }
        monitoringService?.sendCodewordSubmit(input.trim())
    }

    fun onEndCallFromRiskDecision() {
        monitoringService?.sendUserDecision(UserDecisionChoice.TUTUP)
        endCall()
    }

    fun onContinueAtOwnRisk() {
        monitoringService?.sendUserDecision(UserDecisionChoice.LANJUT_RISIKO_SENDIRI)
    }

    fun onDeclineDuringCall() {
        monitoringService?.endSession()
        endCall()
    }

    private fun handleServerEvent(event: CallServerEvent) {
        when (event) {
            is CallServerEvent.SessionStarted -> Unit

            is CallServerEvent.AnalysisResult -> if (event.isSuspicious) {
                _uiState.update {
                    it.copy(
                        stage = CallStage.SUSPICIOUS_FLAGGED,
                        confidence = (event.confidence * 100).roundToInt(),
                        reasoning = event.reason
                    )
                }
            }

            CallServerEvent.RequestVoiceCheck ->
                _uiState.update { it.copy(stage = CallStage.AWAITING_VOICE_CHECK) }

            CallServerEvent.HighRiskWarning ->
                _uiState.update { it.copy(stage = CallStage.AWAITING_RISK_DECISION) }

            CallServerEvent.RequestCodeword -> _uiState.update {
                it.copy(
                    stage = CallStage.CODEWORD_PROMPT,
                    codewordInput = "",
                    codewordError = null,
                    triesLeft = CODEWORD_MAX_TRIES
                )
            }

            is CallServerEvent.CodewordResult -> handleCodewordResult(event)

            CallServerEvent.ResumedMonitoring -> {
                _uiState.update { it.copy(stage = CallStage.IN_CALL_MONITORING, resumedMonitoringBanner = true) }
                viewModelScope.launch {
                    delay(RESUMED_BANNER_DISPLAY_MS)
                    _uiState.update { it.copy(resumedMonitoringBanner = false) }
                }
            }

            is CallServerEvent.SessionSummary -> {
                _uiState.update { it.copy(stage = CallStage.SESSION_SUMMARY, verificationStatus = event.verificationStatus) }
                viewModelScope.launch {
                    delay(SESSION_SUMMARY_DISPLAY_MS)
                    endCall()
                }
            }

            is CallServerEvent.Error -> _uiState.update { it.copy(connectionError = event.detail) }

            CallServerEvent.CallEndedLocally -> endCall()
        }
    }

    private fun handleCodewordResult(event: CallServerEvent.CodewordResult) {
        if (event.verified) {
            _uiState.update { it.copy(isVerifyingCodeword = false, stage = CallStage.CODEWORD_VERIFIED) }
            viewModelScope.launch {
                delay(CODEWORD_VERIFIED_DISPLAY_MS)
                if (_uiState.value.stage == CallStage.CODEWORD_VERIFIED) {
                    _uiState.update { it.copy(stage = CallStage.IN_CALL_MONITORING) }
                }
            }
            return
        }

        val retriesLeft = event.retriesLeft ?: 0
        _uiState.update {
            it.copy(
                isVerifyingCodeword = false,
                triesLeft = retriesLeft,
                codewordInput = "",
                codewordError = "Incorrect codeword.",
                stage = if (retriesLeft <= 0) CallStage.AWAITING_RISK_DECISION else it.stage
            )
        }
    }

    private fun endCall() {
        callTimerJob?.cancel()
        monitoringService?.stopMonitoring()
        unbindServiceIfNeeded()
        _uiState.update { it.copy(stage = CallStage.ENDED) }
    }

    private fun unbindServiceIfNeeded() {
        if (bound) {
            eventsJob?.cancel()
            getApplication<Application>().unbindService(connection)
            bound = false
        }
    }

    private fun startCallTimer() {
        callTimerJob?.cancel()
        callTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _uiState.update { it.copy(callElapsedSeconds = it.callElapsedSeconds + 1) }
            }
        }
    }

    override fun onCleared() {
        callTimerJob?.cancel()
        unbindServiceIfNeeded()
    }
}

class CallOverlayViewModelFactory(
    private val application: Application,
    private val callerNumber: String
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        return CallOverlayViewModel(application, callerNumber) as T
    }
}
