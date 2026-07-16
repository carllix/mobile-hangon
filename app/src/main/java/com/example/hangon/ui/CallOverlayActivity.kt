package com.example.hangon.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hangon.ui.screens.CallOverlayScreen
import com.example.hangon.ui.theme.HangOnTheme
import com.example.hangon.ui.viewmodel.CallOverlayViewModel
import com.example.hangon.ui.viewmodel.CallOverlayViewModelFactory

/**
 * Transparent Activity that sits on top of the Android phone call screen.
 * Launched when CallScreeningService detects an unrecognized incoming number.
 * Uses a transparent window theme (Theme.Transparent in themes.xml).
 */
class CallOverlayActivity : ComponentActivity() {

    private val viewModel: CallOverlayViewModel by viewModels {
        CallOverlayViewModelFactory(intent?.getStringExtra("caller_number") ?: "+1 2345 678 90")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HangOnTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()
                CallOverlayScreen(
                    callerNumber = uiState.callerNumber,
                    onAcceptWithHangOn = {
                        viewModel.onAcceptWithHangOn()
                        finish()
                    },
                    onSkip = {
                        viewModel.onSkip()
                        finish()
                    }
                )
            }
        }
    }
}
