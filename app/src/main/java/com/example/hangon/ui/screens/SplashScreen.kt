package com.example.hangon.ui.screens

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangon.ui.theme.HangOnBlue
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(onSplashComplete: () -> Unit) {
    val infiniteTransition = rememberInfiniteTransition(label = "splash_anim")

    // Scale pulse for logo icon
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "logo_scale"
    )

    // Alpha fade-in for text (from 0 to 1 over 800ms, then stays at 1)
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(800, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "text_alpha"
    )

    // Clamp alpha — only want to animate once but InfiniteTransition keeps going,
    // so use a coerced value
    val clampedAlpha = textAlpha.coerceIn(0f, 1f)

    LaunchedEffect(Unit) {
        delay(2400)
        onSplashComplete()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(HangOnBlue),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.offset(y = (-20).dp)
        ) {
            // Logo icon with pulse animation
            Icon(
                imageVector = Icons.Filled.Call,
                contentDescription = "HangOn Logo",
                tint = Color.White,
                modifier = Modifier
                    .size(72.dp)
                    .scale(scale)
            )

            Spacer(modifier = Modifier.height(20.dp))

            // "HangOn" text with underline (matches reference)
            Text(
                text = "HangOn",
                color = Color.White,
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                textDecoration = TextDecoration.Underline
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Faint reflection / mirror text
            Text(
                text = "HangOn",
                color = Color.White.copy(alpha = 0.15f),
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}
