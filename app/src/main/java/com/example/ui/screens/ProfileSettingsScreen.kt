package com.example.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.AvatarUtils
import com.example.data.model.UserStats
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.LightGrey
import com.example.ui.theme.MediumGrey
import com.example.ui.theme.NeonAqua
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WorkoutViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileSettingsScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.userStats.collectAsState()

    var showEditProfileDialog by remember { mutableStateOf(false) }
    var audioFeedbackEnabled by remember { mutableStateOf(true) }
    var notificationWarningEnabled by remember { mutableStateOf(true) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. HEADER PROFILE INFORMATION CARD ---
        item {
            Column {
                Text(
                    text = "SLAYER PROFILE",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Character customization & settings",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        item {
            stats?.let { uStats ->
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .background(Color(0xFF161618))
                        .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(24.dp))
                        .padding(20.dp)
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Box(
                            modifier = Modifier
                                .size(96.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF242428))
                                .border(2.dp, NeonGreen, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = when (uStats.avatarName) {
                                    "avatar_ninja" -> "🥷"
                                    "avatar_knight" -> "🛡️"
                                    "avatar_mage" -> "🔮"
                                    "avatar_barbarian" -> "🪓"
                                    "avatar_ranger" -> "🏹"
                                    "avatar_cyber" -> "👾"
                                    else -> "🥷"
                                },
                                fontSize = 48.sp
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Text(
                            text = uStats.name.uppercase(),
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 22.sp,
                            letterSpacing = 1.sp
                        )

                        Text(
                            text = uStats.title,
                            color = NeonAqua,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )

                        Spacer(modifier = Modifier.height(14.dp))

                        Button(
                            onClick = { showEditProfileDialog = true },
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF242428), contentColor = TextPrimary),
                            shape = RoundedCornerShape(10.dp),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(36.dp)
                                .testTag("edit_profile_button")
                        ) {
                            Icon(imageVector = Icons.Default.Edit, contentDescription = "Edit", modifier = Modifier.size(14.dp).testTag("edit_profile_icon"))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("CUSTOMIZE CHARACTER", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }

        // --- 2. GAME STATISTICS CARD ---
        item {
            Text(
                text = "SYSTEM SETTINGS",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
        }

        // Audio and Notification Toggles
        item {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161618))
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp))
                    .padding(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.VolumeUp, contentDescription = "sound", tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SYNTH CHIMES / TONAL REWARDS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Play dynamic synth alerts on level up", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    Switch(
                        checked = audioFeedbackEnabled,
                        onCheckedChange = { audioFeedbackEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(imageVector = Icons.Default.NotificationsActive, contentDescription = "alert", tint = NeonGreen, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text("SMART REMINDERS", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                            Text("Remind active streaks & daily protocols", color = TextSecondary, fontSize = 9.sp)
                        }
                    }
                    Switch(
                        checked = notificationWarningEnabled,
                        onCheckedChange = { notificationWarningEnabled = it },
                        colors = SwitchDefaults.colors(checkedThumbColor = Color.Black, checkedTrackColor = NeonGreen)
                    )
                }
            }
        }

        // --- 3. MONETIZATION / PREVIEW UPGRADE BANNERS ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonAqua.copy(0.4f), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0x0C00FFCC))
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(NeonAqua.copy(0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(imageVector = Icons.Default.Shield, contentDescription = "crown", tint = NeonAqua, modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "UNLEASH BATTLE PASS EXTRA", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Unlocks premium attributes, custom avatar shields and advanced charts telemetry.", color = TextSecondary, fontSize = 10.sp)
                    }
                }
            }
        }

        // --- 4. SIGN OUT / DISCONNECT SERVICES ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { /* Sign Out Mock */ }
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(text = "GOOGLE CLOUD DISCONNECT", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text(text = "Sign out linked Google Workspace credentials", color = TextSecondary, fontSize = 9.sp)
                    }
                    Icon(imageVector = Icons.Default.LockOpen, contentDescription = "Sign out", tint = Color.Red.copy(alpha = 0.8f), modifier = Modifier.size(16.dp))
                }
            }
        }

        // --- 5. SYSTEM TECHNICAL CREDITS ---
        item {
            Text(
                text = "VERSION V1.0.4 PROTOTYPE • RECOLLECTIVE CODEX DEPLOYED OK",
                color = TextSecondary,
                fontSize = 8.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    // Swappable Avatar customization dialog
    if (showEditProfileDialog && stats != null) {
        var nicknameInput by remember { mutableStateOf(stats!!.name) }
        var selectedAvatarIndex by remember { mutableStateOf(AvatarUtils.AVATARS.indexOfFirst { it.first == stats!!.avatarName }.coerceAtLeast(0)) }

        AlertDialog(
            onDismissRequest = { showEditProfileDialog = false },
            title = {
                Text(
                    text = "REFORGE SLAYER PROTOCOL",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = nicknameInput,
                        onValueChange = { nicknameInput = it },
                        label = { Text("SLAYER DESIGNATION (NICKNAME)", color = TextSecondary) },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonGreen,
                            unfocusedBorderColor = Color(0xFF2C2C32),
                            focusedLabelColor = NeonGreen
                        ),
                        modifier = Modifier.fillMaxWidth().testTag("edit_nickname_input")
                    )

                    Text(
                        text = "SWAP HERO CLASS AVATAR",
                        color = TextSecondary,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(modifier = Modifier.height(130.dp)) {
                        LazyVerticalGrid(
                            columns = GridCells.Fixed(3),
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            itemsIndexed(AvatarUtils.AVATARS) { index, pair ->
                                val isSelected = selectedAvatarIndex == index
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(if (isSelected) NeonGreen.copy(0.15f) else Color(0xFF242428))
                                        .border(1.dp, if (isSelected) NeonGreen else Color(0xFF2C2C32), RoundedCornerShape(10.dp))
                                        .clickable { selectedAvatarIndex = index }
                                        .padding(8.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(text = pair.second.take(2), fontSize = 24.sp)
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(text = pair.second.drop(2), color = if (isSelected) NeonGreen else TextSecondary, fontSize = 8.sp, maxLines = 1, textAlign = TextAlign.Center)
                                    }
                                }
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val avatarName = AvatarUtils.AVATARS[selectedAvatarIndex].first
                        viewModel.updateProfile(avatarName, nicknameInput)
                        showEditProfileDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_profile_edits")
                ) {
                    Text(text = "APPLY CUSTOMIZATION", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showEditProfileDialog = false }) {
                    Text(text = "ABORT", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF161618)
        )
    }
}
