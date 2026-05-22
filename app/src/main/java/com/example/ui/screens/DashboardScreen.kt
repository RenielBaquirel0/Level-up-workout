package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.DailyQuest
import com.example.data.model.UserStats
import com.example.ui.theme.DarkGrey
import com.example.ui.theme.GlowYellow
import com.example.ui.theme.LightGrey
import com.example.ui.theme.MediumGrey
import com.example.ui.theme.NeonAqua
import com.example.ui.theme.NeonGreen
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.viewmodel.WorkoutViewModel
import java.util.Locale
import kotlinx.coroutines.delay

@Composable
fun DashboardScreen(
    viewModel: WorkoutViewModel,
    onNavigateToTab: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.userStats.collectAsState()
    val quests by viewModel.quests.collectAsState()

    var showQuickDialog by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. SLAYER HEADER CARD ---
        item {
            stats?.let { uStats ->
                AnimatedContentWrapper {
                    SlayerHeaderCard(stats = uStats)
                }
            } ?: Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(140.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color(0xFF161618))
            )
        }

        // --- 2. STATS OVERVIEW GRIDS ---
        item {
            stats?.let { uStats ->
                AnimatedContentWrapper(delayMillis = 100) {
                    RPGStatsGrid(stats = uStats, onInfoClick = { showQuickDialog = true })
                }
            }
        }

        // --- 3. DYNAMIC STREAK BANNER ---
        item {
            stats?.let { uStats ->
                AnimatedContentWrapper(delayMillis = 200) {
                    StreakBanner(
                        currentStreak = uStats.currentStreak,
                        longestStreak = uStats.longestStreak,
                        onBannerClick = { onNavigateToTab(3) } // navigates to quests tab which houses streak calendar
                    )
                }
            }
        }

        // --- 4. DAILY QUESTS CONTAINER (QUICK HOOK) ---
        item {
            AnimatedContentWrapper(delayMillis = 300) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(20.dp))
                        .background(Color(0xFF161618))
                        .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(20.dp))
                        .padding(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "DAILY QUESTS",
                                color = TextPrimary,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 2.sp
                            )
                            Text(
                                text = "Gain experience & expand stats daily",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }
                        IconButton(
                            onClick = { onNavigateToTab(3) },
                            modifier = Modifier.testTag("all_quests_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ChevronRight,
                                contentDescription = "View all quests",
                                tint = NeonGreen
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    val activeQuests = quests.filter { !it.isCompleted }.take(2)
                    if (activeQuests.isEmpty()) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "✨ ALL DAILY QUESTS CONQUERED! ✨",
                                color = NeonGreen,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                        }
                    } else {
                        activeQuests.forEach { quest ->
                            QuickQuestItem(
                                quest = quest,
                                onLogClick = {
                                    if (quest.title.contains("water", ignoreCase = true)) {
                                        viewModel.recordHydrationWater()
                                    } else if (quest.title.contains("stretch", ignoreCase = true)) {
                                        viewModel.recordStretchingMinutes()
                                    } else {
                                        viewModel.completeQuestItemDirectly(quest.title)
                                    }
                                }
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // --- 5. CALL TO ACTION: EXPEDITIONS ---
        item {
            AnimatedContentWrapper(delayMillis = 400) {
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { onNavigateToTab(1) } // Goes to Workout tracker
                        .testTag("launch_workout_card"),
                    colors = CardDefaults.cardColors(containerColor = Color.Transparent),
                    border = CardStrokeGlowBorder(NeonGreen)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(
                                        Color(0xFF161618),
                                        Color(0x2200FF66)
                                    )
                                )
                            )
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2C32)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(text = "🛡️", fontSize = 24.sp)
                        }

                        Spacer(modifier = Modifier.width(16.dp))

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "INITIATE EXPEDITION",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Select an existing exercise routine or create custom protocols to farm EXP.",
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }
                }
            }
        }
    }

    if (showQuickDialog) {
        AlertDialog(
            onDismissRequest = { showQuickDialog = false },
            title = {
                Text(
                    text = "RPG ATTRIBUTE INFO",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp,
                    letterSpacing = 1.sp
                )
            },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    AttributeExplainRow(title = "⚙️ STR (Strength)", desc = "Raised via Strength workouts (eg squats, bench presses). Boosts physical capacity.")
                    AttributeExplainRow(title = "⚡ END (Endurance)", desc = "Earned through cardio and multi-set volume. Enhances long-term recovery attributes.")
                    AttributeExplainRow(title = "🔮 DSC (Discipline)", desc = "Earned by logging scheduled routines and keeping daily hydration and stretch tasks.")
                    AttributeExplainRow(title = "🔥 CONS (Consistency)", desc = "Climbs with active daily workout tracking and logging body parameters regularly.")
                    AttributeExplainRow(title = "🏃 AGI (Agility)", desc = "Honed by HIIT sessions, reflex sprint drills and Core speed training.")
                }
            },
            confirmButton = {
                TextButton(onClick = { showQuickDialog = false }) {
                    Text(text = "DISMISS", color = NeonGreen)
                }
            },
            containerColor = Color(0xFF161618),
            titleContentColor = TextPrimary,
            textContentColor = TextSecondary
        )
    }
}

// --- HELPER COMPOSABLES ---

@Composable
fun SlayerHeaderCard(stats: UserStats) {
    val animatedProgress by animateFloatAsState(
        targetValue = stats.xp.toFloat() / stats.xpNeeded.toFloat(),
        animationSpec = tween(durationMillis = 1000)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(Color(0xFF161618))
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(24.dp))
            .padding(20.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Interactive character avatar view
            CharacterAvatarIcon(avatarName = stats.avatarName, level = stats.level)

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = stats.name.uppercase(),
                    color = TextPrimary,
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 1.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    text = stats.title,
                    color = NeonAqua,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "EXP: ${stats.xp} / ${stats.xpNeeded} XP",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }

            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF2C2C32))
                    .border(1.dp, NeonGreen, RoundedCornerShape(12.dp))
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "LVL",
                        color = TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = stats.level.toString(),
                        color = NeonGreen,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Level Up Progress Bar with glowing neon fill
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF2C2C32))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(CircleShape)
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                NeonGreen,
                                NeonAqua
                            )
                        )
                    )
            )
        }
    }
}

@Composable
fun CharacterAvatarIcon(avatarName: String, level: Int) {
    val emoji = when (avatarName) {
        "avatar_ninja" -> "🥷"
        "avatar_knight" -> "🛡️"
        "avatar_mage" -> "🔮"
        "avatar_barbarian" -> "🪓"
        "avatar_ranger" -> "🏹"
        "avatar_cyber" -> "👾"
        else -> "🥷"
    }

    Box(
        modifier = Modifier
            .size(68.dp)
            .clip(CircleShape)
            .background(Color(0xFF242428))
            .border(2.dp, NeonGreen, CircleShape)
            .shadow(4.dp, CircleShape),
        contentAlignment = Alignment.Center
    ) {
        Text(text = emoji, fontSize = 34.sp)
    }
}

@Composable
fun RPGStatsGrid(stats: UserStats, onInfoClick: () -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "CHARACTER ATTRIBUTES",
                color = TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.5.sp
            )
            Text(
                text = "What is this?",
                color = NeonGreen,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onInfoClick() }
                    .padding(4.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatItemCard(
                title = "STR",
                stat = stats.strength,
                emoji = "⚙️",
                color = NeonGreen,
                modifier = Modifier.weight(1f)
            )
            StatItemCard(
                title = "END",
                stat = stats.endurance,
                emoji = "⚡",
                color = NeonAqua,
                modifier = Modifier.weight(1f)
            )
            StatItemCard(
                title = "DSC",
                stat = stats.discipline,
                emoji = "🔮",
                color = GlowYellow,
                modifier = Modifier.weight(1f)
            )
        }
        Spacer(modifier = Modifier.height(10.dp))
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            StatItemCard(
                title = "CONS",
                stat = stats.consistency,
                emoji = "🔥",
                color = Color(0xFFFF5E00),
                modifier = Modifier.weight(1f)
            )
            StatItemCard(
                title = "AGI",
                stat = stats.agility,
                emoji = "🏃",
                color = Color(0xFFBF00FF),
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
fun StatItemCard(
    title: String,
    stat: Float,
    emoji: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161618))
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = emoji, fontSize = 16.sp)
                Text(
                    text = title,
                    color = TextSecondary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = String.format(Locale.ROOT, "%.1f", stat),
                color = TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Black
            )
            Spacer(modifier = Modifier.height(4.dp))
            // Progress visual
            val progressFraction = (stat % 10f) / 10f
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF2C2C32))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(if (progressFraction <= 0f) 0.1f else progressFraction)
                        .background(color)
                )
            }
        }
    }
}

@Composable
fun StreakBanner(currentStreak: Int, longestStreak: Int, onBannerClick: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onBannerClick() },
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618)),
        border = BorderStroke(1.dp, Color(0xFF2C2C32))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(Color(0x33FF5E00)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.LocalFireDepartment,
                    contentDescription = "Streak Fire",
                    tint = Color(0xFFFF5E00),
                    modifier = Modifier.size(26.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "DISCIPLINE STREAK: $currentStreak DAYS",
                    color = TextPrimary,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Primal high streak record stands at $longestStreak days.",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
            Icon(
                imageVector = Icons.Default.FlashOn,
                contentDescription = "Activity Link",
                tint = GlowYellow,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun QuickQuestItem(quest: DailyQuest, onLogClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF242428))
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = quest.title,
                color = TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = "${quest.progressCurrent}/${quest.progressTarget} completed • +${quest.xpReward} EXP",
                color = NeonGreen,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Button(
            onClick = onLogClick,
            colors = ButtonDefaults.buttonColors(
                containerColor = Color(0xFF2C2C32),
                contentColor = TextPrimary
            ),
            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
            shape = RoundedCornerShape(8.dp),
            modifier = Modifier
                .height(32.dp)
                .testTag("log_quest_${quest.title.replace(" ", "_")}")
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = if (quest.title.contains("water", ignoreCase = true)) Icons.Default.WaterDrop else Icons.Default.Add,
                    contentDescription = "Quick complete",
                    tint = NeonGreen,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (quest.title.contains("water", ignoreCase = true)) "LOG L" else "LOG",
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun AttributeExplainRow(title: String, desc: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(text = title, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)
        Text(text = desc, color = TextSecondary, fontSize = 11.sp)
        Spacer(modifier = Modifier.height(4.dp))
    }
}

// Custom glow border builder suitable for premium style matching
fun CardStrokeGlowBorder(color: Color): BorderStroke {
    return BorderStroke(1.dp, Brush.radialGradient(listOf(color, Color.Transparent, Color(0xFF2C2C32))))
}

// Fade in slide up transitional wrapper for visual satisfaction
@Composable
fun AnimatedContentWrapper(
    delayMillis: Int = 0,
    content: @Composable () -> Unit
) {
    var visible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(delayMillis.toLong())
        visible = true
    }
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(600)) + slideInVertically(
            initialOffsetY = { 30 },
            animationSpec = tween(600)
        )
    ) {
        content()
    }
}
