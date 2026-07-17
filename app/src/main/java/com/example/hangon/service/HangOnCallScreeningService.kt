package com.example.hangon.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import androidx.core.app.NotificationCompat
import com.example.hangon.R
import com.example.hangon.data.local.AppPrefs
import com.example.hangon.data.util.ContactsHelper
import com.example.hangon.ui.CallOverlayActivity

class HangOnCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())

        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return
        if (!AppPrefs.isAppActivated) return
        if (ContactsHelper.isKnownContact(applicationContext, phoneNumber)) return

        showFullScreenCallAlert(phoneNumber)
    }

    private fun showFullScreenCallAlert(phoneNumber: String) {
        createNotificationChannel()

        val overlayIntent = Intent(applicationContext, CallOverlayActivity::class.java).apply {
            putExtra(CallOverlayActivity.EXTRA_CALLER_NUMBER, phoneNumber)
            putExtra(CallOverlayActivity.EXTRA_NOTIFICATION_ID, NOTIFICATION_ID)
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }

        val fullScreenPendingIntent = PendingIntent.getActivity(
            applicationContext,
            NOTIFICATION_ID,
            overlayIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Nomor tidak dikenal")
            .setContentText(phoneNumber)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_CALL)
            .setFullScreenIntent(fullScreenPendingIntent, true)
            .setContentIntent(fullScreenPendingIntent)
            .setAutoCancel(true)
            .build()

        getSystemService(NotificationManager::class.java).notify(NOTIFICATION_ID, notification)
    }

    private fun createNotificationChannel() {
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Peringatan Panggilan Masuk",
            NotificationManager.IMPORTANCE_HIGH
        )
        getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
    }

    companion object {
        private const val CHANNEL_ID = "incoming_call_alert"
        private const val NOTIFICATION_ID = 2001
    }
}
