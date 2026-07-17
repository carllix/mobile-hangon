package com.example.hangon.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.GppBad
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.hangon.ui.theme.DangerRed
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.HangOnBlueDark
import com.example.hangon.ui.theme.SuccessGreen
import com.example.hangon.ui.theme.SurfaceWhite
import com.example.hangon.ui.theme.TextPrimary
import com.example.hangon.ui.theme.TextSecondary
import com.example.hangon.ui.viewmodel.CallOverlayUiState
import com.example.hangon.ui.viewmodel.CallOverlayViewModel
import com.example.hangon.ui.viewmodel.CallStage

private val CallBackgroundColor = Color(0xFF1A2340)

@Composable
fun CallOverlayScreen(
    viewModel: CallOverlayViewModel,
    onFinished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.stage) {
        if (uiState.stage == CallStage.ENDED) {
            onFinished()
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        if (uiState.stage == CallStage.RINGING_CONSENT) {
            CallConfirmationCard(
                onYes = viewModel::onAcceptWithHangOn,
                onNo = viewModel::onSkip
            )
        } else {
            Box(modifier = Modifier.fillMaxWidth()) {
                MonitoringStatusChip(
                    elapsedSeconds = uiState.callElapsedSeconds,
                    onStop = viewModel::onDeclineDuringCall,
                    modifier = Modifier.align(Alignment.CenterEnd)
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            when (uiState.stage) {
                CallStage.SUSPICIOUS_FLAGGED -> SuspiciousActivityCard(
                    uiState = uiState,
                    onContinue = viewModel::onContinueAfterSuspicious,
                    onEndCall = viewModel::onEndCallFromSuspicious
                )
                CallStage.AWAITING_VOICE_CHECK -> VoiceCheckCard(
                    onResponse = viewModel::onVoiceCheckResponse
                )
                CallStage.CODEWORD_PROMPT -> CodewordPromptCard(
                    onRequestInput = viewModel::onRequestCodewordInput
                )
                CallStage.CODEWORD_INPUT -> CodewordInputCard(
                    uiState = uiState,
                    onInputChange = viewModel::onCodewordInputChange,
                    onVerify = viewModel::onVerifyCodeword
                )
                CallStage.CODEWORD_VERIFIED -> CodewordVerifiedCard()
                CallStage.AWAITING_RISK_DECISION -> HighRiskWarningCard(
                    onEndCall = viewModel::onEndCallFromRiskDecision,
                    onContinueAtOwnRisk = viewModel::onContinueAtOwnRisk
                )
                CallStage.SESSION_SUMMARY -> SessionSummaryCard(verificationStatus = uiState.verificationStatus)
                else -> Unit
            }

            if (uiState.resumedMonitoringBanner) {
                ResumedMonitoringBanner()
            }

            if (uiState.stage == CallStage.IN_CALL_MONITORING && uiState.connectionError != null) {
                ConnectionErrorBanner(message = uiState.connectionError.orEmpty())
            }
        }
    }
}

@Composable
private fun MonitoringStatusChip(elapsedSeconds: Int, onStop: () -> Unit, modifier: Modifier = Modifier) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    Surface(
        modifier = modifier.padding(end = 16.dp),
        shape = RoundedCornerShape(20.dp),
        color = CallBackgroundColor.copy(alpha = 0.9f)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text(timeText, style = MaterialTheme.typography.labelSmall, color = Color.White)
            Spacer(modifier = Modifier.width(10.dp))
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(DangerRed)
                    .clickable(onClick = onStop),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "End HangOn Monitoring",
                    tint = Color.White,
                    modifier = Modifier.size(12.dp)
                )
            }
        }
    }
}

@Composable
fun CallConfirmationCard(onYes: () -> Unit, onNo: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
    ) {
        OverlayCardShell {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        Brush.linearGradient(listOf(HangOnBlue, HangOnBlueDark)),
                        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
                    )
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Unrecognized Number", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Would you like HangOn to\naccompany this call?",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Keep an eye on your screen for guidance and\nscam alerts during the call.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onNo,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.5.dp, TextSecondary.copy(alpha = 0.4f))
                ) {
                    Text("No", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, textAlign = TextAlign.Center)
                }

                Button(
                    onClick = onYes,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun SuspiciousActivityCard(
    uiState: CallOverlayUiState,
    onContinue: () -> Unit,
    onEndCall: () -> Unit
) {
    AnimatedOverlayCard {
        OverlayCardShell {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DangerRed, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 20.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Suspicious Activity Detected", style = MaterialTheme.typography.titleSmall, color = Color.White, fontWeight = FontWeight.SemiBold)
                Text("${uiState.confidence}% SURE", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Do you\nwant to continue?",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                Text("REASONING", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 0.5.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFFFDF3D8)
                ) {
                    Text(
                        uiState.reasoning,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF7A5B12),
                        lineHeight = 18.sp,
                        modifier = Modifier.padding(12.dp)
                    )
                }

                if (uiState.flaggedKeywords.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text("FLAGGED KEYWORDS", style = MaterialTheme.typography.labelSmall, color = TextSecondary, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(8.dp))
                    FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        uiState.flaggedKeywords.forEach { keyword ->
                            KeywordChip(keyword)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onContinue,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = BorderStroke(1.5.dp, TextSecondary.copy(alpha = 0.4f))
                ) {
                    Text("Continue", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, textAlign = TextAlign.Center)
                }

                Button(
                    onClick = onEndCall,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("End Call", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun KeywordChip(keyword: String) {
    Surface(
        shape = RoundedCornerShape(50),
        color = DangerRed.copy(alpha = 0.1f),
        border = BorderStroke(1.dp, DangerRed.copy(alpha = 0.3f))
    ) {
        Text(
            text = keyword,
            style = MaterialTheme.typography.labelSmall,
            color = DangerRed,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )
    }
}

@Composable
private fun CodewordPromptCard(onRequestInput: () -> Unit) {
    AnimatedOverlayCard {
        OverlayCardShell {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .background(HangOnBlue, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ask the caller to\nsay the code word",
                style = MaterialTheme.typography.headlineSmall,
                color = HangOnBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Example: \"Can you say our code word?\"",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontStyle = FontStyle.Italic,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onRequestInput,
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).height(50.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
            ) {
                Text(
                    "Type the code word they say",
                    fontWeight = FontWeight.SemiBold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CodewordInputCard(
    uiState: CallOverlayUiState,
    onInputChange: (String) -> Unit,
    onVerify: () -> Unit
) {
    AnimatedOverlayCard {
        OverlayCardShell {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HangOnBlue, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(horizontal = 20.dp, vertical = 10.dp),
                contentAlignment = Alignment.CenterEnd
            ) {
                Text("${uiState.triesLeft}/2 TRIES", style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Ask the caller to\nsay the code word",
                style = MaterialTheme.typography.headlineSmall,
                color = HangOnBlue,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(20.dp))

            Column(modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp)) {
                OutlinedTextField(
                    value = uiState.codewordInput,
                    onValueChange = onInputChange,
                    placeholder = { Text("Type code word here...") },
                    singleLine = true,
                    isError = uiState.codewordError != null,
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HangOnBlue),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )
                if (uiState.codewordError != null) {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(uiState.codewordError, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = onVerify,
                    enabled = uiState.codewordInput.isNotBlank() && !uiState.isVerifyingCodeword,
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
                ) {
                    if (uiState.isVerifyingCodeword) {
                        CircularProgressIndicator(modifier = Modifier.size(20.dp), color = Color.White, strokeWidth = 2.dp)
                    } else {
                        Text(
                            "Verify Code Word",
                            fontWeight = FontWeight.SemiBold,
                            color = Color.White,
                            textAlign = TextAlign.Center
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun CodewordVerifiedCard() {
    AnimatedOverlayCard {
        OverlayCardShell {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = SuccessGreen, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text("Code Word Verified", style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "The caller is confirmed. You can continue the call safely.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun VoiceCheckCard(onResponse: (recognized: Boolean) -> Unit) {
    AnimatedOverlayCard {
        OverlayCardShell {
            Spacer(modifier = Modifier.height(28.dp))

            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(HangOnBlue.copy(alpha = 0.1f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.RecordVoiceOver, contentDescription = null, tint = HangOnBlue, modifier = Modifier.size(32.dp))
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Do you recognize\nthis caller's voice?",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "This helps us tell a real contact apart from a cloned voice.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = { onResponse(false) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextSecondary),
                    border = BorderStroke(1.5.dp, TextSecondary.copy(alpha = 0.4f))
                ) {
                    Text("No, I don't", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, textAlign = TextAlign.Center)
                }

                Button(
                    onClick = { onResponse(true) },
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
                ) {
                    Text("Yes, I know it", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun HighRiskWarningCard(onEndCall: () -> Unit, onContinueAtOwnRisk: () -> Unit) {
    AnimatedOverlayCard {
        OverlayCardShell {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(DangerRed, shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp))
                    .padding(vertical = 14.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("High Risk Call", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.SemiBold)
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Unrecognized voice +\nsuspicious topic",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimary,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "This call shows strong signs of a scam. We recommend ending it now.",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 24.dp),
                lineHeight = 18.sp
            )

            Spacer(modifier = Modifier.height(24.dp))

            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp).padding(bottom = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onContinueAtOwnRisk,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = TextPrimary),
                    border = BorderStroke(1.5.dp, TextSecondary.copy(alpha = 0.4f))
                ) {
                    Text("Continue Anyway", fontWeight = FontWeight.SemiBold, fontSize = 14.sp, textAlign = TextAlign.Center)
                }

                Button(
                    onClick = onEndCall,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("End Call", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White, textAlign = TextAlign.Center)
                }
            }
        }
    }
}

@Composable
private fun SessionSummaryCard(verificationStatus: String?) {
    val (icon, tint, label) = when (verificationStatus) {
        "terverifikasi" -> Triple(Icons.Filled.VerifiedUser, SuccessGreen, "Caller Verified")
        "gagal" -> Triple(Icons.Filled.GppBad, DangerRed, "Verification Failed")
        else -> Triple(Icons.Filled.Shield, TextSecondary, "Call Monitoring Ended")
    }

    AnimatedOverlayCard {
        OverlayCardShell {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(56.dp))
                Spacer(modifier = Modifier.height(16.dp))
                Text(label, style = MaterialTheme.typography.headlineSmall, color = TextPrimary, fontWeight = FontWeight.Bold)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "This call's protection summary has been saved.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 24.dp)
                )
            }
        }
    }
}

@Composable
private fun ResumedMonitoringBanner() {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = RoundedCornerShape(14.dp),
        color = HangOnBlueDark
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                "Monitoring resumed at your own risk",
                style = MaterialTheme.typography.bodySmall,
                color = Color.White,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ConnectionErrorBanner(message: String) {
    Surface(
        modifier = Modifier.padding(horizontal = 24.dp),
        shape = RoundedCornerShape(14.dp),
        color = DangerRed
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(Icons.Filled.Warning, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Text(message, style = MaterialTheme.typography.bodySmall, color = Color.White, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun AnimatedOverlayCard(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn()
    ) {
        content()
    }
}

@Composable
private fun OverlayCardShell(content: @Composable androidx.compose.foundation.layout.ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 20.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
            content()
        }
    }
}
