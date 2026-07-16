package com.example.hangon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.hangon.ui.navigation.HangOnNavGraph
import com.example.hangon.ui.theme.HangOnTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Install the SplashScreen API splash (shows the static icon from themes.xml briefly)
        installSplashScreen()

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            HangOnTheme {
                // Full navigation graph: Splash → Permission Setup → Home / Family
                HangOnNavGraph()
            }
        }
    }
}