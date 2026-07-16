package com.example.hangon.ui

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import com.example.hangon.ui.screens.CallOverlayScreen
import com.example.hangon.ui.theme.HangOnTheme
import com.example.hangon.ui.viewmodel.CallOverlayViewModel
import com.example.hangon.ui.viewmodel.CallOverlayViewModelFactory

class CallOverlayActivity : ComponentActivity() {

    private val viewModel: CallOverlayViewModel by viewModels {
        CallOverlayViewModelFactory(intent?.getStringExtra("caller_number") ?: "+1 2345 678 90")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            HangOnTheme {
                CallOverlayScreen(viewModel = viewModel, onFinished = ::finish)
            }
        }
    }
}
