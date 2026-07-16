package com.example.hangon.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangon.ui.theme.*

data class PermissionStatus(
    val icon: ImageVector,
    val title: String,
    val description: String,
    val isRequired: Boolean,
    val isGranted: Boolean
)

@Composable
fun HomeScreen() {
    var appActivated by remember { mutableStateOf(true) }

    val permissions = remember {
        mutableStateListOf(
            PermissionStatus(
                icon = Icons.Filled.Contacts,
                title = "Contact Access",
                description = "Mencocokkan nomor masuk dengan kontak Anda untuk deteksi lebih akurat.",
                isRequired = false,
                isGranted = true
            ),
            PermissionStatus(
                icon = Icons.Filled.Mic,
                title = "Call Audio Access",
                description = "Diperlukan untuk merekam dan menganalisis audio selama panggilan berlangsung.",
                isRequired = true,
                isGranted = true
            ),
            PermissionStatus(
                icon = Icons.Filled.Layers,
                title = "Display Over Other Apps",
                description = "Diperlukan untuk menampilkan overlay peringatan saat ada panggilan masuk.",
                isRequired = true,
                isGranted = false
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo + App Name Header
        Icon(
            imageVector = Icons.Filled.Call,
            contentDescription = "HangOn Logo",
            tint = HangOnBlue,
            modifier = Modifier.size(52.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "HangOn",
            style = MaterialTheme.typography.headlineLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(28.dp))

        // Activate App Card
        ActivateAppCard(isActivated = appActivated, onToggle = { appActivated = it })

        Spacer(modifier = Modifier.height(28.dp))

        // Permission Settings Section
        Text(
            text = "Permission Settings",
            style = MaterialTheme.typography.headlineSmall,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp)
        )

        // Permission cards in a card container (like reference)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            permissions.forEachIndexed { index, perm ->
                HomePermissionRow(
                    icon = perm.icon,
                    title = perm.title,
                    description = perm.description,
                    isRequired = perm.isRequired,
                    isGranted = perm.isGranted,
                    onToggle = { permissions[index] = perm.copy(isGranted = it) }
                )
                if (index < permissions.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = DividerColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Status summary banner
        StatusBanner(appActivated = appActivated, permissions = permissions)
    }
}

@Composable
fun ActivateAppCard(isActivated: Boolean, onToggle: (Boolean) -> Unit) {
    val gradientColors = if (isActivated) {
        listOf(HangOnBlue, HangOnBlueDark)
    } else {
        listOf(TextSecondary, TextSecondary.copy(alpha = 0.7f))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActivated) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.linearGradient(gradientColors))
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // Pulsing green dot when active
                        if (isActivated) {
                            PulsingDot()
                            Spacer(modifier = Modifier.width(8.dp))
                        }
                        Text(
                            text = "Activate App",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isActivated)
                            "HangOn aktif sebagai call screening app."
                        else
                            "Aktifkan HangOn sebagai call screening app.",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.85f)
                    )
                }

                Switch(
                    checked = isActivated,
                    onCheckedChange = onToggle,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = HangOnBlue,
                        checkedTrackColor = Color.White,
                        uncheckedThumbColor = Color.White.copy(alpha = 0.8f),
                        uncheckedTrackColor = Color.White.copy(alpha = 0.3f),
                        uncheckedBorderColor = Color.White.copy(alpha = 0.3f)
                    )
                )
            }
        }
    }
}

@Composable
fun PulsingDot() {
    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val alpha by infiniteTransition.animateFloat(
        initialValue = 0.4f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(700, easing = EaseInOutSine),
            repeatMode = RepeatMode.Reverse
        ),
        label = "dot_alpha"
    )
    Box(
        modifier = Modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(SuccessGreen.copy(alpha = alpha))
    )
}

@Composable
fun HomePermissionRow(
    icon: ImageVector,
    title: String,
    description: String,
    isRequired: Boolean,
    isGranted: Boolean,
    onToggle: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(if (isGranted) HangOnBlue.copy(alpha = 0.1f) else DividerColor),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isGranted) HangOnBlue else TextSecondary,
                modifier = Modifier.size(20.dp)
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.SemiBold
                )
                if (!isRequired) {
                    Spacer(modifier = Modifier.width(6.dp))
                    Surface(
                        shape = RoundedCornerShape(4.dp),
                        color = WarningOrange.copy(alpha = 0.12f)
                    ) {
                        Text(
                            text = "Opsional",
                            style = MaterialTheme.typography.labelMedium,
                            color = WarningOrange,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 15.sp
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Switch(
            checked = isGranted,
            onCheckedChange = onToggle,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = HangOnBlue,
                uncheckedThumbColor = Color.White,
                uncheckedTrackColor = DividerColor,
                uncheckedBorderColor = DividerColor
            )
        )
    }
}

@Composable
fun StatusBanner(
    appActivated: Boolean,
    permissions: List<PermissionStatus>
) {
    val missingRequired = permissions.filter { it.isRequired && !it.isGranted }

    val (bgColor, textColor, icon, message) = when {
        !appActivated -> Quadruple(
            TextSecondary.copy(alpha = 0.1f),
            TextSecondary,
            Icons.Filled.PauseCircle,
            "HangOn tidak aktif. Aktifkan untuk mulai melindungi Anda."
        )
        missingRequired.isNotEmpty() -> Quadruple(
            DangerRed.copy(alpha = 0.08f),
            DangerRed,
            Icons.Filled.Warning,
            "Izin wajib belum diberikan: ${missingRequired.joinToString(", ") { it.title }}"
        )
        else -> Quadruple(
            SuccessGreen.copy(alpha = 0.08f),
            SuccessGreen,
            Icons.Filled.VerifiedUser,
            "HangOn aktif dan siap melindungi panggilan Anda."
        )
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = bgColor
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = textColor,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = message,
                style = MaterialTheme.typography.bodySmall,
                color = textColor,
                lineHeight = 18.sp
            )
        }
    }
}

// Utility data class for destructuring
data class Quadruple<A, B, C, D>(val first: A, val second: B, val third: C, val fourth: D)
