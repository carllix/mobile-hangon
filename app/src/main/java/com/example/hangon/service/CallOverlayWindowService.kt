package com.example.hangon.service

import android.app.Service
import android.content.Intent
import android.graphics.PixelFormat
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.WindowManager
import androidx.compose.ui.platform.ComposeView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.setViewTreeLifecycleOwner
import androidx.lifecycle.setViewTreeViewModelStoreOwner
import androidx.savedstate.setViewTreeSavedStateRegistryOwner
import com.example.hangon.ui.screens.CallOverlayScreen
import com.example.hangon.ui.theme.HangOnTheme
import com.example.hangon.ui.viewmodel.CallOverlayViewModel
import com.example.hangon.ui.viewmodel.CallOverlayViewModelFactory

class CallOverlayWindowService : Service() {

    private var overlayView: ComposeView? = null
    private var lifecycleOwner: OverlayLifecycleOwner? = null

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val callerNumber = intent?.getStringExtra(EXTRA_CALLER_NUMBER)
        if (overlayView == null && callerNumber != null && Settings.canDrawOverlays(this)) {
            showOverlay(callerNumber)
        } else if (overlayView == null) {
            stopSelf()
        }
        return START_NOT_STICKY
    }

    @Suppress("DEPRECATION")
    private fun showOverlay(callerNumber: String) {
        val owner = OverlayLifecycleOwner().also { it.start() }
        lifecycleOwner = owner

        val viewModel = ViewModelProvider(
            owner,
            CallOverlayViewModelFactory(application = application, callerNumber = callerNumber)
        )[CallOverlayViewModel::class.java]

        val composeView = ComposeView(this).apply {
            setViewTreeLifecycleOwner(owner)
            setViewTreeViewModelStoreOwner(owner)
            setViewTreeSavedStateRegistryOwner(owner)
            setContent {
                HangOnTheme {
                    CallOverlayScreen(
                        viewModel = viewModel,
                        onFinished = { stopSelf() }
                    )
                }
            }
        }
        overlayView = composeView

        val layoutParams = WindowManager.LayoutParams(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY,
            WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL or
                WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN or
                WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or
                WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON or
                WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD,
            PixelFormat.TRANSLUCENT
        ).apply {
            gravity = Gravity.TOP
        }

        getSystemService(WindowManager::class.java).addView(composeView, layoutParams)
    }

    override fun onDestroy() {
        overlayView?.let {
            runCatching { getSystemService(WindowManager::class.java).removeView(it) }
        }
        overlayView = null
        lifecycleOwner?.stop()
        lifecycleOwner = null
        super.onDestroy()
    }

    companion object {
        const val EXTRA_CALLER_NUMBER = "caller_number"
    }
}
