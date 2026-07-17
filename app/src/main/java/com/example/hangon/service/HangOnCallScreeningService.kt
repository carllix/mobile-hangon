package com.example.hangon.service

import android.content.Intent
import android.telecom.Call
import android.telecom.CallScreeningService
import com.example.hangon.data.local.AppPrefs
import com.example.hangon.data.util.ContactsHelper

class HangOnCallScreeningService : CallScreeningService() {

    override fun onScreenCall(callDetails: Call.Details) {
        respondToCall(callDetails, CallResponse.Builder().build())

        val phoneNumber = callDetails.handle?.schemeSpecificPart ?: return
        if (!AppPrefs.isAppActivated) return
        if (ContactsHelper.isKnownContact(applicationContext, phoneNumber)) return

        val overlayIntent = Intent(applicationContext, CallOverlayWindowService::class.java).apply {
            putExtra(CallOverlayWindowService.EXTRA_CALLER_NUMBER, phoneNumber)
        }
        startService(overlayIntent)
    }
}
