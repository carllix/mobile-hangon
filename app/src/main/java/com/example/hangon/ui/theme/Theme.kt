package com.example.hangon.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val HangOnColorScheme = lightColorScheme(
    primary = HangOnBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = HangOnBlueAlpha,
    onPrimaryContainer = HangOnBlueDark,
    secondary = HangOnBlueLight,
    onSecondary = SurfaceWhite,
    background = BackgroundLight,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceCard,
    onSurfaceVariant = TextSecondary,
    outline = DividerColor,
    error = DangerRed,
    onError = SurfaceWhite,
)

@Composable
fun HangOnTheme(
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = HangOnColorScheme,
        typography = Typography,
        content = content
    )
}