package com.example.hangon.ui.screens

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import com.example.hangon.R
import com.example.hangon.data.util.PermissionRequests
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.PauseCircle
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
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
import com.example.hangon.ui.viewmodel.HomeViewModel

// --- Home Screen ---
@Composable
fun HomeScreen(viewModel: HomeViewModel = viewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissionHandler = rememberPermissionRowHandler()
    val context = LocalContext.current
    val callScreeningLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { viewModel.refresh() }
    RefreshOnResume(onResume = viewModel::refresh)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo + App Name Header (image version)
        Image(
            painter = painterResource(id = R.drawable.ic_logo_text),
            contentDescription = "HangOn Logo",
            contentScale = androidx.compose.ui.layout.ContentScale.Fit,
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp)
        )

        Spacer(modifier = Modifier.height(36.dp))

        // Activate App Card
        ActivateAppCard(
            isActivated = uiState.appActivated,
            onToggle = { activated ->
                if (activated && !viewModel.isCallScreeningGranted()) {
                    PermissionRequests.specialPermissionIntentFor(context, "call_screening")
                        ?.let { callScreeningLauncher.launch(it) }
                } else {
                    viewModel.setLocalActivation(activated)
                }
            }
        )

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
                    onToggle = { permissionHandler(perm) }
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

        Spacer(modifier = Modifier.height(16.dp))

        // Status summary banner
        //StatusBanner(appActivated = uiState.appActivated, permissions = uiState.permissions)
    }
}

// --- Activate App Card ---
@Composable
fun ActivateAppCard(isActivated: Boolean, onToggle: (Boolean) -> Unit) {
    val cardColor = if (isActivated) HangOnBlue else TextSecondary

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = if (isActivated) 6.dp else 2.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(cardColor)
                .padding(horizontal = 20.dp, vertical = 20.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = "Activate App",
                            style = MaterialTheme.typography.titleLarge,
                            color = Color.White,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = " *",
                            style = MaterialTheme.typography.titleLarge,
                            color = DangerRed,
                            fontWeight = FontWeight.Bold
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = if (isActivated)
                            "HangOn is active as your call screening app."
                        else
                            "Activate HangOn as your call screening app.",
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
            .padding(horizontal = 16.dp, vertical = 20.dp),
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
                if (isRequired) {
                    Text(
                        text = " *",
                        style = MaterialTheme.typography.titleMedium,
                        color = DangerRed,
                        fontWeight = FontWeight.Bold
                    )
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
            message = "HangOn is inactive. Activate it to start protecting you."
        }
        missingRequired.isNotEmpty() -> {
            bgColor = DangerRed.copy(alpha = 0.08f)
            textColor = DangerRed
            icon = Icons.Filled.Warning
            message = "Required permissions not granted: ${missingRequired.joinToString(", ") { it.title }}"
        }
        else -> {
            bgColor = SuccessGreen.copy(alpha = 0.08f)
            textColor = SuccessGreen
            icon = Icons.Filled.VerifiedUser
            message = "HangOn is active and ready to protect your calls."
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

