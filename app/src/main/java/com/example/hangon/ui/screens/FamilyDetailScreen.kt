package com.example.hangon.ui.screens

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Key
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.example.hangon.data.model.FamilyMember
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
import com.example.hangon.ui.viewmodel.FamilyDetailViewModel

@Composable
fun FamilyDetailScreen(
    familyId: String,
    onBack: () -> Unit,
    viewModel: FamilyDetailViewModel = viewModel(
        factory = viewModelFactory {
            initializer { FamilyDetailViewModel(familyId = familyId) }
        }
    )
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val clipboardManager = LocalClipboardManager.current

    LaunchedEffect(uiState.leftFamily) {
        if (uiState.leftFamily) onBack()
    }

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 40.dp, start = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                }
                Text(
                    text = uiState.family?.name ?: "",
                    style = MaterialTheme.typography.headlineSmall,
                    color = HangOnBlue,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1
                )
                Spacer(modifier = Modifier.width(48.dp))
            }

            when {
                uiState.isLoading && uiState.family == null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = HangOnBlue)
                    }
                }
                uiState.family != null -> {
                    FamilyDetailContent(
                        familyName = uiState.family!!.name,
                        inviteCode = uiState.family!!.inviteCode,
                        members = uiState.family!!.members,
                        myRole = uiState.family!!.myRole,
                        currentUserId = uiState.currentUserId,
                        currentCodeword = uiState.currentCodeword,
                        secondsLeft = uiState.secondsLeft,
                        isLeaving = uiState.isLeaving,
                        onCopyInviteCode = {
                            clipboardManager.setText(AnnotatedString(uiState.family!!.inviteCode))
                        },
                        onRequestRemoveMember = viewModel::onRequestRemoveMember,
                        onLeaveFamily = viewModel::onLeaveFamily
                    )
                }
                uiState.errorMessage != null -> {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Text(uiState.errorMessage.orEmpty(), color = DangerRed)
                    }
                }
            }
        }

        if (uiState.memberPendingRemoval != null) {
            AlertDialog(
                onDismissRequest = viewModel::onCancelRemoveMember,
                containerColor = SurfaceWhite,
                shape = RoundedCornerShape(24.dp),
                title = { Text("Remove Member?", fontWeight = FontWeight.Bold, color = TextPrimary) },
                text = {
                    Text(
                        "This member will no longer be able to generate a valid codeword for this family.",
                        color = TextSecondary
                    )
                },
                confirmButton = {
                    Button(
                        onClick = viewModel::onConfirmRemoveMember,
                        colors = ButtonDefaults.buttonColors(containerColor = DangerRed)
                    ) {
                        Text("Remove", fontWeight = FontWeight.SemiBold)
                    }
                },
                dismissButton = {
                    TextButton(onClick = viewModel::onCancelRemoveMember) {
                        Text("Cancel", color = TextSecondary)
                    }
                }
            )
        }
    }
}

@Composable
private fun FamilyDetailContent(
    familyName: String,
    inviteCode: String,
    members: List<FamilyMember>,
    myRole: String,
    currentUserId: String?,
    currentCodeword: String,
    secondsLeft: Int,
    isLeaving: Boolean,
    onCopyInviteCode: () -> Unit,
    onRequestRemoveMember: (String) -> Unit,
    onLeaveFamily: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 12.dp, bottom = 24.dp)
    ) {
        InviteCodeCard(inviteCode = inviteCode, onCopy = onCopyInviteCode)

        Spacer(modifier = Modifier.height(16.dp))

        if (currentCodeword.isNotBlank()) {
            CodewordCard(codeword = currentCodeword, secondsLeft = secondsLeft)
            Spacer(modifier = Modifier.height(20.dp))
        }

        Text(
            text = "Family Member",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            members.forEachIndexed { index, member ->
                MemberRow(
                    member = member,
                    canRemove = myRole == "owner" && member.userId != currentUserId,
                    onRemove = { onRequestRemoveMember(member.userId) }
                )
                if (index < members.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = 1.dp,
                        color = DividerColor
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(28.dp))

        Button(
            onClick = onLeaveFamily,
            enabled = !isLeaving,
            modifier = Modifier.fillMaxWidth().height(52.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = DangerRed.copy(alpha = 0.08f),
                contentColor = DangerRed
            )
        ) {
            if (isLeaving) {
                CircularProgressIndicator(modifier = Modifier.size(18.dp), color = DangerRed, strokeWidth = 2.dp)
            } else {
                Text("Leave Family", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun InviteCodeCard(inviteCode: String, onCopy: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(HangOnBlue)
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text("Family Code", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(SurfaceWhite)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = inviteCode.toCharArray().joinToString(" "),
                        style = MaterialTheme.typography.headlineSmall,
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Send this invitation code to someone you trust.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
                IconButton(
                    onClick = onCopy,
                    modifier = Modifier
                        .size(40.dp)
                        .background(HangOnBlue.copy(alpha = 0.08f), RoundedCornerShape(10.dp))
                ) {
                    Icon(Icons.Filled.ContentCopy, contentDescription = "Copy code", tint = HangOnBlue, modifier = Modifier.size(18.dp))
                }
            }
        }
    }
}

@Composable
private fun CodewordCard(codeword: String, secondsLeft: Int) {
    val progress = secondsLeft / 60f
    val progressColor = when {
        secondsLeft > 30 -> SuccessGreen
        secondsLeft > 10 -> WarningOrange
        else -> DangerRed
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(androidx.compose.ui.graphics.Brush.linearGradient(listOf(HangOnBlue, HangOnBlueDark)))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.fillMaxWidth()) {
                    Icon(Icons.Filled.Key, contentDescription = null, tint = Color.White.copy(alpha = 0.7f), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "ACTIVE CODEWORD",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    Surface(shape = RoundedCornerShape(8.dp), color = progressColor.copy(alpha = 0.2f)) {
                        Text(
                            text = "${secondsLeft}s",
                            style = MaterialTheme.typography.labelMedium,
                            color = progressColor,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                AnimatedContent(
                    targetState = codeword,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith (slideOutVertically { it } + fadeOut())
                    },
                    label = "codeword_anim"
                ) { word ->
                    Text(
                        text = word,
                        style = MaterialTheme.typography.displaySmall,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(4.dp),
                    color = progressColor,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Read this codeword aloud if someone claims to be a family member during a call.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
private fun MemberRow(member: FamilyMember, canRemove: Boolean, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = member.displayName,
            style = MaterialTheme.typography.titleMedium,
            color = TextPrimary,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        if (member.role == "owner") {
            Surface(shape = RoundedCornerShape(6.dp), color = HangOnBlue.copy(alpha = 0.1f)) {
                Text(
                    text = "Admin",
                    style = MaterialTheme.typography.labelSmall,
                    color = HangOnBlue,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                )
            }
        }

        if (canRemove) {
            Spacer(modifier = Modifier.width(8.dp))
            IconButton(onClick = onRemove, modifier = Modifier.size(28.dp)) {
                Icon(Icons.Filled.Close, contentDescription = "Remove member", tint = DangerRed, modifier = Modifier.size(16.dp))
            }
        }
    }
}
