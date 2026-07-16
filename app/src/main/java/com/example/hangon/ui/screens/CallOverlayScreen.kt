package com.example.hangon.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dialpad
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Videocam
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hangon.ui.theme.DangerRed
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.HangOnBlueDark
import com.example.hangon.ui.theme.SuccessGreen
import com.example.hangon.ui.theme.SurfaceWhite
import com.example.hangon.ui.theme.TextPrimary
import com.example.hangon.ui.theme.TextSecondary
import com.example.hangon.ui.viewmodel.CallOverlayUiState
import com.example.hangon.ui.viewmodel.CallOverlayViewModel
import com.example.hangon.ui.viewmodel.CallOverlayViewModelFactory
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(CallBackgroundColor.copy(alpha = 0.95f))
    ) {
        if (uiState.stage == CallStage.RINGING_CONSENT) {
            IncomingCallBackground(callerNumber = uiState.callerNumber)
            CallConfirmationCard(
                onYes = viewModel::onAcceptWithHangOn,
                onNo = viewModel::onSkip
            )
        } else {
            InCallBackground(elapsedSeconds = uiState.callElapsedSeconds, onDecline = viewModel::onDeclineDuringCall)

            when (uiState.stage) {
                CallStage.SUSPICIOUS_FLAGGED -> SuspiciousActivityCard(
                    uiState = uiState,
                    onContinue = viewModel::onContinueAfterSuspicious,
                    onEndCall = viewModel::onEndCallFromSuspicious
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
                else -> Unit
            }
        }
    }
}

@Composable
fun IncomingCallBackground(callerNumber: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(80.dp))

        Text(
            text = callerNumber,
            style = MaterialTheme.typography.displayLarge,
            color = Color.White,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.Center,
            fontSize = 34.sp
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.12f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(54.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.height(360.dp))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 60.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ChevronIndicator()
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.size(68.dp).clip(CircleShape).background(SuccessGreen),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.Call, contentDescription = "Accept", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Accept", style = MaterialTheme.typography.bodySmall, color = Color.White)
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                ChevronIndicator()
                Spacer(modifier = Modifier.height(6.dp))
                Box(
                    modifier = Modifier.size(68.dp).clip(CircleShape).background(DangerRed),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.CallEnd, contentDescription = "Decline", tint = Color.White, modifier = Modifier.size(30.dp))
                }
                Spacer(modifier = Modifier.height(6.dp))
                Text("Decline", style = MaterialTheme.typography.bodySmall, color = Color.White)
            }
        }
    }
}

@Composable
fun ChevronIndicator() {
    val infiniteTransition = rememberInfiniteTransition(label = "chevron")
    val offsetY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = -6f,
        animationSpec = infiniteRepeatable(animation = tween(600, easing = EaseInOutSine), repeatMode = RepeatMode.Reverse),
        label = "chevron_offset"
    )

    Column(modifier = Modifier.offset(y = offsetY.dp)) {
        repeat(2) {
            Icon(
                imageVector = Icons.Filled.KeyboardArrowUp,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.5f),
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun InCallBackground(elapsedSeconds: Int, onDecline: () -> Unit) {
    val minutes = elapsedSeconds / 60
    val seconds = elapsedSeconds % 60
    val timeText = "%02d:%02d".format(minutes, seconds)

    Column(
        modifier = Modifier.fillMaxSize().padding(horizontal = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(60.dp))

        Text(
            text = timeText,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Box(
            modifier = Modifier.size(100.dp).clip(CircleShape).background(Color.White),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = CallBackgroundColor.copy(alpha = 0.85f),
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 340.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            InCallIconButton(icon = Icons.Filled.Videocam, label = "video")
            InCallIconButton(icon = Icons.Filled.Dialpad, label = "keypad")
            InCallIconButton(icon = Icons.Filled.Pause, label = "hold")
        }

        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(bottom = 60.dp)) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(DangerRed)
                    .clickable(onClick = onDecline),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.CallEnd,
                    contentDescription = "Decline",
                    tint = Color.White,
                    modifier = Modifier.size(30.dp)
                )
            }
            Spacer(modifier = Modifier.height(6.dp))
            Text("Decline", style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}

@Composable
private fun InCallIconButton(icon: androidx.compose.ui.graphics.vector.ImageVector, label: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(Color.Transparent),
            contentAlignment = Alignment.Center
        ) {
            Surface(
                shape = CircleShape,
                color = Color.Transparent,
                border = BorderStroke(1.5.dp, Color.White.copy(alpha = 0.5f)),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(icon, contentDescription = label, tint = Color.White, modifier = Modifier.size(22.dp))
                }
            }
        }
        Spacer(modifier = Modifier.height(6.dp))
        Text(label, style = MaterialTheme.typography.bodySmall, color = Color.White.copy(alpha = 0.8f))
    }
}

@Composable
fun BoxScope.CallConfirmationCard(onYes: () -> Unit, onNo: () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        modifier = Modifier.align(Alignment.Center)
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
                    Text("No", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Button(
                    onClick = onYes,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
                ) {
                    Text("Yes", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.SuspiciousActivityCard(
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
                Text(
                    "SUSPICIOUS KEYWORD",
                    style = MaterialTheme.typography.labelSmall,
                    color = TextSecondary,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    uiState.suspiciousKeywords.forEach { keyword ->
                        Surface(shape = RoundedCornerShape(8.dp), color = DangerRed.copy(alpha = 0.1f)) {
                            Text(
                                keyword,
                                style = MaterialTheme.typography.labelMedium,
                                color = DangerRed,
                                fontWeight = FontWeight.SemiBold,
                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                    Text("Continue", fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                }

                Button(
                    onClick = onEndCall,
                    modifier = Modifier.weight(1f).height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                ) {
                    Text("End Call", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
private fun BoxScope.CodewordPromptCard(onRequestInput: () -> Unit) {
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
                Text("Type the code word they say", fontWeight = FontWeight.SemiBold, color = Color.White)
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BoxScope.CodewordInputCard(
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
                        Text("Verify Code Word", fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BoxScope.CodewordVerifiedCard() {
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
private fun BoxScope.AnimatedOverlayCard(content: @Composable () -> Unit) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) { visible = true }

    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(initialOffsetY = { it / 2 }, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)) + fadeIn(),
        modifier = Modifier.align(Alignment.Center)
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

@Composable
fun CallOverlayPreviewScreen(onDismiss: () -> Unit) {
    val viewModel: CallOverlayViewModel = viewModel(
        factory = CallOverlayViewModelFactory("+1 1234 5678 90")
    )

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, dismissOnClickOutside = false)
    ) {
        CallOverlayScreen(viewModel = viewModel, onFinished = onDismiss)
    }
}
