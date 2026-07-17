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
import androidx.compose.material.icons.outlined.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hangon.data.model.Permission
import com.example.hangon.ui.theme.*
import com.example.hangon.ui.viewmodel.PermissionSetupViewModel

private fun permissionIcon(id: String): ImageVector = when (id) {
    "call_screening" -> Icons.Filled.Call
    "audio" -> Icons.Filled.Mic
    "overlay" -> Icons.Filled.Layers
    "contacts" -> Icons.Filled.Contacts
    else -> Icons.Filled.Layers
}

@Composable
fun PermissionSetupScreen(
    onSetupComplete: () -> Unit,
    viewModel: PermissionSetupViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val permissions = uiState.permissions
    val allRequiredGranted = uiState.allRequiredGranted
    val permissionHandler = rememberPermissionRowHandler()
    RefreshOnResume(onResume = viewModel::refresh)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundLight)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp)
                .padding(top = 56.dp, bottom = 120.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header illustration
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(HangOnBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Security,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(44.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Required Permissions",
                style = MaterialTheme.typography.headlineMedium,
                color = TextPrimary,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "HangOn needs a few permissions to protect you from phone scams in real time.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            // Permission cards
            permissions.forEach { permission ->
                PermissionCard(
                    permission = permission,
                    onGranted = { permissionHandler(permission) }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }

        // Bottom CTA
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .background(
                    Brush.verticalGradient(
                        colors = listOf(Color.Transparent, BackgroundLight),
                        startY = 0f
                    )
                )
                .padding(horizontal = 24.dp, vertical = 24.dp)
        ) {
            Button(
                onClick = { if (allRequiredGranted) onSetupComplete() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                enabled = allRequiredGranted,
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = HangOnBlue,
                    disabledContainerColor = HangOnBlue.copy(alpha = 0.4f),
                    contentColor = Color.White,
                    disabledContentColor = Color.White.copy(alpha = 0.6f)
                )
            ) {
                Icon(
                    imageVector = Icons.Filled.Shield,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (allRequiredGranted) "Start Protection" else "Grant Required Permissions",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun PermissionCard(
    permission: Permission,
    onGranted: () -> Unit
) {
    val borderColor = if (permission.isGranted) HangOnBlue.copy(alpha = 0.5f) else DividerColor
    val bgColor = if (permission.isGranted) HangOnBlue.copy(alpha = 0.04f) else SurfaceWhite

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, borderColor, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Icon box
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (permission.isGranted) HangOnBlue
                        else HangOnBlue.copy(alpha = 0.1f)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = permissionIcon(permission.id),
                    contentDescription = null,
                    tint = if (permission.isGranted) Color.White else HangOnBlue,
                    modifier = Modifier.size(22.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = permission.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                    if (!permission.isRequired) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Surface(
                            shape = RoundedCornerShape(4.dp),
                            color = WarningOrange.copy(alpha = 0.12f)
                        ) {
                            Text(
                                text = "Optional",
                                style = MaterialTheme.typography.labelMedium,
                                color = WarningOrange,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = permission.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary,
                    lineHeight = 16.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Switch(
                checked = permission.isGranted,
                onCheckedChange = { onGranted() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = HangOnBlue,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = DividerColor
                )
            )
        }
    }
}
