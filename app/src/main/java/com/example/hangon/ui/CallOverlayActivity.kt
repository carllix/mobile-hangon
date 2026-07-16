package com.example.hangon.ui

import android.app.Activity
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.hangon.ui.screens.CallOverlayScreen
import com.example.hangon.ui.theme.HangOnTheme

/**
 * Transparent Activity that sits on top of the Android phone call screen.
 * Launched when CallScreeningService detects an unrecognized incoming number.
 * Uses a transparent window theme (Theme.Transparent in themes.xml).
 */
class CallOverlayActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val callerNumber = intent?.getStringExtra("caller_number") ?: "+1 2345 678 90"

        setContent {
            HangOnTheme {
                CallOverlayScreen(
                    callerNumber = callerNumber,
                    onAcceptWithHangOn = {
                        // In production: start HangOn monitoring service, then finish
                        finish()
                    },
                    onSkip = {
                        finish()
                    }
                )
            }
        }
    }
}
