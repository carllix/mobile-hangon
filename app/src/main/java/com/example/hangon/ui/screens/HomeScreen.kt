package com.example.hangon.ui.screens

import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hangon.data.model.Permission
import com.example.hangon.ui.theme.BackgroundLight
import com.example.hangon.ui.theme.DangerRed
import com.example.hangon.ui.theme.DividerColor
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.HangOnBlueDark
import com.example.hangon.ui.theme.SuccessGreen
import com.example.hangon.ui.theme.SurfaceWhite
import com.example.hangon.ui.theme.TextPrimary
import com.example.hangon.ui.theme.TextSecondary
import com.example.hangon.ui.theme.WarningOrange
import com.example.hangon.ui.viewmodel.HomeViewModel

// --- Home Screen ---
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    if (uiState.showCallSimulation) {
        CallOverlayPreviewScreen(onDismiss = { viewModel.onShowCallSimulation(false) })
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
        ActivateAppCard(isActivated = uiState.appActivated, onToggle = viewModel::onAppActivatedChange)

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

        // Permission cards in a grouped card (matching reference design)
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            uiState.permissions.forEachIndexed { index, perm ->
                HomePermissionRow(
                    icon = when (perm.id) {
                        "contacts" -> Icons.Filled.Contacts
                        "audio" -> Icons.Filled.Mic
                        else -> Icons.Filled.Layers
                    },
                    title = perm.title,
                    description = perm.description,
                    isRequired = perm.isRequired,
                    isGranted = perm.isGranted,
                    onToggle = { viewModel.onPermissionToggle(index, it) }
                )
                if (index < uiState.permissions.lastIndex) {
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
        StatusBanner(appActivated = uiState.appActivated, permissions = uiState.permissions)

        Spacer(modifier = Modifier.height(20.dp))

        // ── Simulate incoming call button (for testing overlay without backend) ──
        SimulateCallButton(onClick = { viewModel.onShowCallSimulation(true) })
    }
}

// --- Activate App Card ---
@Composable
fun ActivateAppCard(isActivated: Boolean, onToggle: (Boolean) -> Unit) {
    val gradientColors = if (isActivated) {
        listOf(HangOnBlue, HangOnBlueDark)
    } else {
        listOf(TextSecondary, TextSecondary.copy(alpha = 0.7f))
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
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

// --- Permission Row ---
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

// --- Status Banner ---
@Composable
fun StatusBanner(appActivated: Boolean, permissions: List<Permission>) {
    val missingRequired = permissions.filter { it.isRequired && !it.isGranted }

    val bgColor: Color
    val textColor: Color
    val icon: ImageVector
    val message: String

    when {
        !appActivated -> {
            bgColor = TextSecondary.copy(alpha = 0.1f)
            textColor = TextSecondary
            icon = Icons.Filled.PauseCircle
            message = "HangOn tidak aktif. Aktifkan untuk mulai melindungi Anda."
        }
        missingRequired.isNotEmpty() -> {
            bgColor = DangerRed.copy(alpha = 0.08f)
            textColor = DangerRed
            icon = Icons.Filled.Warning
            message = "Izin wajib belum diberikan: ${missingRequired.joinToString(", ") { it.title }}"
        }
        else -> {
            bgColor = SuccessGreen.copy(alpha = 0.08f)
            textColor = SuccessGreen
            icon = Icons.Filled.VerifiedUser
            message = "HangOn aktif dan siap melindungi panggilan Anda."
        }
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

// --- Simulate Incoming Call Button ---
@Composable
fun SimulateCallButton(onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = HangOnBlue.copy(alpha = 0.06f)
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.Info,
                    contentDescription = null,
                    tint = HangOnBlue,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "Mode Pengembangan",
                    style = MaterialTheme.typography.labelMedium,
                    color = HangOnBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "Tekan tombol di bawah untuk melihat tampilan popup saat ada panggilan masuk dari nomor tidak dikenal.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                lineHeight = 16.sp
            )
            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onClick,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
            ) {
                Icon(
                    Icons.Filled.Call,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Simulasi Panggilan Masuk",
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}
