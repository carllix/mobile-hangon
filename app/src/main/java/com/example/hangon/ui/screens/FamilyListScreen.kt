package com.example.hangon.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.AlertDialog
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.hangon.data.model.FamilySummary
import com.example.hangon.ui.theme.BackgroundLight
import com.example.hangon.ui.theme.DangerRed
import com.example.hangon.ui.theme.HangOnBlue
import com.example.hangon.ui.theme.SurfaceWhite
import com.example.hangon.ui.theme.TextPrimary
import com.example.hangon.ui.theme.TextSecondary
import com.example.hangon.ui.viewmodel.FamilyListViewModel

@Composable
fun FamilyListScreen(
    onFamilyClick: (String) -> Unit,
    viewModel: FamilyListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    RefreshOnResume(onResume = viewModel::loadFamilies)

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        when {
            uiState.isLoading && uiState.families.isEmpty() -> {
                CircularProgressIndicator(
                    modifier = Modifier.align(Alignment.Center),
                    color = HangOnBlue
                )
            }
            uiState.families.isEmpty() -> {
                NoFamilyView(
                    onCreateFamily = { viewModel.onShowCreateDialog(true) },
                    onJoinFamily = { viewModel.onShowJoinDialog(true) }
                )
            }
            else -> {
                FamilyListContent(
                    families = uiState.families,
                    onFamilyClick = onFamilyClick,
                    onCreateFamily = { viewModel.onShowCreateDialog(true) },
                    onJoinFamily = { viewModel.onShowJoinDialog(true) }
                )
            }
        }

        if (uiState.showJoinDialog) {
            JoinFamilyDialog(
                isSubmitting = uiState.isSubmitting,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.onShowJoinDialog(false) },
                onJoin = viewModel::onJoinFamily
            )
        }
        if (uiState.showCreateDialog) {
            CreateFamilyDialog(
                isSubmitting = uiState.isSubmitting,
                errorMessage = uiState.errorMessage,
                onDismiss = { viewModel.onShowCreateDialog(false) },
                onCreate = viewModel::onCreateFamily
            )
        }
    }
}

@Composable
private fun FamilyListContent(
    families: List<FamilySummary>,
    onFamilyClick: (String) -> Unit,
    onCreateFamily: () -> Unit,
    onJoinFamily: () -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "My Family",
            style = MaterialTheme.typography.headlineMedium,
            color = HangOnBlue,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 56.dp, bottom = 20.dp)
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            items(families, key = { it.id }) { family ->
                FamilyCard(family = family, onClick = { onFamilyClick(family.id) })
            }
        }

        Column(modifier = Modifier.padding(20.dp)) {
            OutlinedButton(
                onClick = onJoinFamily,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.5.dp, HangOnBlue),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = HangOnBlue)
            ) {
                Text("Join Using Family Code", fontWeight = FontWeight.SemiBold)
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = onCreateFamily,
                modifier = Modifier.fillMaxWidth().height(52.dp),
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
            ) {
                Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Add New Family", fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Composable
private fun FamilyCard(family: FamilySummary, onClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = family.name,
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = family.memberPreviewText(),
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }
            Icon(
                Icons.Filled.ChevronRight,
                contentDescription = null,
                tint = TextSecondary.copy(alpha = 0.6f)
            )
        }
    }
}

private fun FamilySummary.memberPreviewText(): String {
    val extra = memberCount - memberPreviewNames.size
    val names = memberPreviewNames.joinToString(", ")
    return if (extra > 0) "$names, and $extra more" else names
}

@Composable
fun NoFamilyView(onCreateFamily: () -> Unit, onJoinFamily: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(120.dp)
                .background(HangOnBlue.copy(alpha = 0.08f), shape = CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Groups,
                contentDescription = null,
                tint = HangOnBlue,
                modifier = Modifier.size(60.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Create or Join a Family",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "A Family is a group of trusted people. A shared codeword is generated every 60 seconds to verify identity during calls.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onCreateFamily,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Add New Family", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onJoinFamily,
            modifier = Modifier.fillMaxWidth().height(54.dp),
            shape = RoundedCornerShape(14.dp),
            border = BorderStroke(1.5.dp, HangOnBlue.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HangOnBlue)
        ) {
            Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Join Using Family Code", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
fun JoinFamilyDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onJoin: (String) -> Unit
) {
    var inviteCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Join Using Family Code",
                fontWeight = FontWeight.Bold,
                color = HangOnBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text("Family Code", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    placeholder = { Text("Enter family code...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HangOnBlue)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, HangOnBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HangOnBlue)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onJoin(inviteCode) },
                    enabled = inviteCode.isNotBlank() && !isSubmitting,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Join", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        dismissButton = null
    )
}

@Composable
fun CreateFamilyDialog(
    isSubmitting: Boolean,
    errorMessage: String?,
    onDismiss: () -> Unit,
    onCreate: (String) -> Unit
) {
    var familyName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        title = {
            Text(
                "Add New Family",
                fontWeight = FontWeight.Bold,
                color = HangOnBlue,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        text = {
            Column {
                Text("Family Name", style = MaterialTheme.typography.labelLarge, color = TextPrimary)
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    placeholder = { Text("Enter family name...") },
                    singleLine = true,
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = HangOnBlue)
                )
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(errorMessage, color = DangerRed, style = MaterialTheme.typography.bodySmall)
                }
            }
        },
        confirmButton = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    border = BorderStroke(1.5.dp, HangOnBlue.copy(alpha = 0.5f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = HangOnBlue)
                ) {
                    Text("Cancel", fontWeight = FontWeight.SemiBold)
                }
                Button(
                    onClick = { onCreate(familyName) },
                    enabled = familyName.isNotBlank() && !isSubmitting,
                    modifier = Modifier.weight(1f).height(48.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
                ) {
                    if (isSubmitting) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = Color.White,
                            strokeWidth = 2.dp
                        )
                    } else {
                        Text("Add", fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        },
        dismissButton = null
    )
}
