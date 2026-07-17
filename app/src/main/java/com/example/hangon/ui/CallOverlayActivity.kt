package com.example.hangon.ui

import android.app.NotificationManager
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
        CallOverlayViewModelFactory(
            application = application,
            callerNumber = intent?.getStringExtra(EXTRA_CALLER_NUMBER) ?: "+1 2345 678 90"
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val notificationId = intent?.getIntExtra(EXTRA_NOTIFICATION_ID, -1) ?: -1
        if (notificationId != -1) {
            getSystemService(NotificationManager::class.java).cancel(notificationId)
        }

        setContent {
            HangOnTheme {
                CallOverlayScreen(viewModel = viewModel, onFinished = ::finish)
            }
        }
    }

    companion object {
        const val EXTRA_CALLER_NUMBER = "caller_number"
        const val EXTRA_NOTIFICATION_ID = "notification_id"
    }
}
