package com.example.hangon.ui.screens

import androidx.compose.animation.*
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.hangon.ui.theme.*
import kotlinx.coroutines.delay

// --- Data Models ---

data class FamilyMember(
    val id: String,
    val name: String,
    val initials: String,
    val phone: String,
    val avatarColor: Color
)

data class FamilyGroup(
    val id: String,
    val name: String,
    val members: List<FamilyMember>,
    val codeword: String,
    val secondsUntilRefresh: Int
)

// --- Family Screen ---

@Composable
fun FamilyScreen() {
    // Dummy state: no family yet or has family
    var hasFamilyGroup by remember { mutableStateOf(false) }
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateDialog by remember { mutableStateOf(false) }

    val dummyFamily = FamilyGroup(
        id = "fam_001",
        name = "Keluarga Santoso",
        members = listOf(
            FamilyMember("1", "Budi Santoso", "BS", "+62 812-3456-7890", HangOnBlue),
            FamilyMember("2", "Siti Santoso", "SS", "+62 821-9876-5432", Color(0xFF7C3AED)),
            FamilyMember("3", "Andi Santoso", "AS", "+62 856-1234-5678", SuccessGreen),
        ),
        codeword = "MANGO-7734",
        secondsUntilRefresh = 42
    )

    Box(modifier = Modifier.fillMaxSize().background(BackgroundLight)) {
        if (hasFamilyGroup) {
            FamilyGroupView(
                family = dummyFamily,
                onLeave = { hasFamilyGroup = false }
            )
        } else {
            NoFamilyView(
                onCreateFamily = { showCreateDialog = true },
                onJoinFamily = { showJoinDialog = true }
            )
        }

        // Dialogs
        if (showJoinDialog) {
            JoinFamilyDialog(
                onDismiss = { showJoinDialog = false },
                onJoin = { showJoinDialog = false; hasFamilyGroup = true }
            )
        }
        if (showCreateDialog) {
            CreateFamilyDialog(
                onDismiss = { showCreateDialog = false },
                onCreate = { showCreateDialog = false; hasFamilyGroup = true }
            )
        }
    }
}

// --- No Family View ---

@Composable
fun NoFamilyView(onCreateFamily: () -> Unit, onJoinFamily: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Illustration
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(HangOnBlue.copy(alpha = 0.08f)),
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
            text = "Buat atau Gabung Family",
            style = MaterialTheme.typography.headlineMedium,
            color = TextPrimary,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Family adalah kelompok orang terpercaya. Codeword bersama akan di-generate setiap 60 detik untuk memverifikasi identitas saat panggilan.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondary,
            textAlign = TextAlign.Center,
            lineHeight = 22.sp
        )

        Spacer(modifier = Modifier.height(40.dp))

        Button(
            onClick = onCreateFamily,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
        ) {
            Icon(Icons.Filled.Add, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Buat Family Baru", fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onJoinFamily,
            modifier = Modifier
                .fillMaxWidth()
                .height(54.dp),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.5.dp, HangOnBlue.copy(alpha = 0.5f)),
            colors = ButtonDefaults.outlinedButtonColors(contentColor = HangOnBlue)
        ) {
            Icon(Icons.Filled.GroupAdd, contentDescription = null, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text("Gabung Family", fontWeight = FontWeight.SemiBold)
        }
    }
}

// --- Family Group View ---

@Composable
fun FamilyGroupView(family: FamilyGroup, onLeave: () -> Unit) {
    // Countdown timer for codeword regeneration
    var secondsLeft by remember { mutableStateOf(family.secondsUntilRefresh) }
    var currentCodeword by remember { mutableStateOf(family.codeword) }

    LaunchedEffect(Unit) {
        while (true) {
            delay(1000)
            secondsLeft = (secondsLeft - 1).coerceAtLeast(0)
            if (secondsLeft == 0) {
                secondsLeft = 60
                // Rotate to a new codeword (simulated)
                currentCodeword = listOf(
                    "TIGER-4421", "SUNSET-8812", "RIVER-6637",
                    "CLOUD-2295", "MANGO-7734", "EAGLE-5549"
                ).random()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(top = 56.dp, bottom = 24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Family",
                    style = MaterialTheme.typography.headlineMedium,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = family.name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
            }
            IconButton(onClick = onLeave) {
                Icon(Icons.Filled.Logout, contentDescription = "Keluar", tint = DangerRed)
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Codeword Card
        CodewordCard(codeword = currentCodeword, secondsLeft = secondsLeft)

        Spacer(modifier = Modifier.height(20.dp))

        // Members Section
        Text(
            text = "Anggota Family",
            style = MaterialTheme.typography.titleLarge,
            color = TextPrimary,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(12.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = SurfaceWhite),
            elevation = CardDefaults.cardElevation(0.dp)
        ) {
            family.members.forEachIndexed { index, member ->
                MemberRow(member = member)
                if (index < family.members.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(start = 68.dp, end = 16.dp),
                        thickness = 1.dp,
                        color = DividerColor
                    )
                }
            }

            // Invite button
            HorizontalDivider(
                modifier = Modifier.padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = DividerColor
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { }
                    .padding(horizontal = 16.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(HangOnBlue.copy(alpha = 0.1f))
                        .border(1.5.dp, HangOnBlue.copy(alpha = 0.3f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Filled.PersonAdd, contentDescription = null, tint = HangOnBlue, modifier = Modifier.size(20.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Text(
                    text = "Undang Anggota",
                    style = MaterialTheme.typography.titleMedium,
                    color = HangOnBlue,
                    fontWeight = FontWeight.SemiBold
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Info card
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            color = HangOnBlue.copy(alpha = 0.06f)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.Top
            ) {
                Icon(Icons.Filled.Info, contentDescription = null, tint = HangOnBlue, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Codeword diperbarui otomatis setiap 60 detik. Jika seseorang mengklaim sebagai anggota family saat menelepon, minta mereka menyebutkan codeword yang aktif saat ini.",
                    style = MaterialTheme.typography.bodySmall,
                    color = HangOnBlue,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

@Composable
fun CodewordCard(codeword: String, secondsLeft: Int) {
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
                .background(Brush.linearGradient(listOf(HangOnBlue, HangOnBlueDark)))
                .padding(20.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(
                        Icons.Filled.Key,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.7f),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "CODEWORD AKTIF",
                        style = MaterialTheme.typography.labelMedium,
                        color = Color.White.copy(alpha = 0.7f),
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.weight(1f))
                    // Timer badge
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = progressColor.copy(alpha = 0.2f)
                    ) {
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

                // Codeword text with animated swap
                AnimatedContent(
                    targetState = codeword,
                    transitionSpec = {
                        (slideInVertically { -it } + fadeIn()) togetherWith
                                (slideOutVertically { it } + fadeOut())
                    },
                    label = "codeword_anim"
                ) { word ->
                    Text(
                        text = word,
                        style = MaterialTheme.typography.displayMedium,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 3.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Progress bar
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(4.dp)
                        .clip(RoundedCornerShape(2.dp)),
                    color = progressColor,
                    trackColor = Color.White.copy(alpha = 0.15f)
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = "Diperbarui dalam $secondsLeft detik",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun MemberRow(member: FamilyMember) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Avatar
        Box(
            modifier = Modifier
                .size(44.dp)
                .clip(CircleShape)
                .background(member.avatarColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = member.initials,
                style = MaterialTheme.typography.titleMedium,
                color = member.avatarColor,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = member.name,
                style = MaterialTheme.typography.titleMedium,
                color = TextPrimary,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                text = member.phone,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }

        Icon(
            Icons.Filled.VerifiedUser,
            contentDescription = "Verified",
            tint = SuccessGreen,
            modifier = Modifier.size(18.dp)
        )
    }
}

// --- Dialogs ---

@Composable
fun JoinFamilyDialog(onDismiss: () -> Unit, onJoin: () -> Unit) {
    var inviteCode by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(Icons.Filled.GroupAdd, contentDescription = null, tint = HangOnBlue, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Gabung Family", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    text = "Masukkan kode undangan yang diberikan oleh anggota family Anda.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = inviteCode,
                    onValueChange = { inviteCode = it.uppercase() },
                    label = { Text("Kode Undangan") },
                    placeholder = { Text("Contoh: FAM-X8K2") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HangOnBlue,
                        focusedLabelColor = HangOnBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onJoin,
                enabled = inviteCode.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
            ) {
                Text("Gabung", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}

@Composable
fun CreateFamilyDialog(onDismiss: () -> Unit, onCreate: () -> Unit) {
    var familyName by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = SurfaceWhite,
        shape = RoundedCornerShape(24.dp),
        icon = {
            Icon(Icons.Filled.Groups, contentDescription = null, tint = HangOnBlue, modifier = Modifier.size(36.dp))
        },
        title = {
            Text("Buat Family Baru", fontWeight = FontWeight.Bold, color = TextPrimary)
        },
        text = {
            Column {
                Text(
                    text = "Beri nama family Anda. Anggota bisa diundang setelah family dibuat.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary
                )
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text("Nama Family") },
                    placeholder = { Text("Contoh: Keluarga Santoso") },
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth(),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = HangOnBlue,
                        focusedLabelColor = HangOnBlue
                    )
                )
            }
        },
        confirmButton = {
            Button(
                onClick = onCreate,
                enabled = familyName.isNotBlank(),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = HangOnBlue)
            ) {
                Text("Buat", fontWeight = FontWeight.SemiBold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal", color = TextSecondary)
            }
        }
    )
}
