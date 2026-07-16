package com.example.hangon

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.example.hangon.ui.navigation.HangOnNavGraph
import com.example.hangon.ui.navigation.Screen
import com.example.hangon.ui.theme.HangOnTheme
import com.google.firebase.auth.FirebaseAuth

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val startDestination = if (FirebaseAuth.getInstance().currentUser != null) {
            Screen.Home.route
        } else {
            Screen.Login.route
        }

        setContent {
            HangOnTheme {
                HangOnNavGraph(startDestination = startDestination)
            }
        }
    }
}
