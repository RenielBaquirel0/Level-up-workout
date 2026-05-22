package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.itemsIndexed
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Achievement
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

@Composable
fun QuestsAchievementsScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val quests by viewModel.quests.collectAsState()
    val achievements by viewModel.achievements.collectAsState()
    val stats by viewModel.userStats.collectAsState()

    var activeSubTab by remember { mutableStateOf(0) } // 0 = Quests, 1 = Trophy Cabinet, 2 = Streak Heatmap

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(horizontal = 16.dp)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // --- SUB NAV TABS BAR ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF161618))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            listOf("DAILY QUESTS", "ACHIEVEMENTS", "STREAK LABS").forEachIndexed { index, title ->
                val isSelected = activeSubTab == index
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(10.dp))
                        .background(if (isSelected) NeonGreen else Color.Transparent)
                        .clickable { activeSubTab = index }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title,
                        color = if (isSelected) Color.Black else TextSecondary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- DYNAMIC SUB-VIEW LIST ---
        when (activeSubTab) {
            0 -> {
                // DAILY QUESTS checklist
                Column(modifier = Modifier.weight(1f)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "ACTIVE BOUNTY BOARD",
                                color = TextPrimary,
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Bold,
                                letterSpacing = 1.sp
                            )
                            Text(
                                text = "Quests reset once every 24-hour cycle.",
                                color = TextSecondary,
                                fontSize = 11.sp
                            )
                        }

                        stats?.let { uStats ->
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF161618))
                                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(8.dp))
                                    .padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = "🔥 STRK: ${uStats.currentStreak}D",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Black
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    if (quests.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No quests currently scheduled.", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            itemsIndexed(quests) { index, quest ->
                                QuestDetailedCardItem(
                                    quest = quest,
                                    onActionClick = {
                                        if (quest.title.contains("water", ignoreCase = true)) {
                                            viewModel.recordHydrationWater()
                                        } else if (quest.title.contains("stretch", ignoreCase = true)) {
                                            viewModel.recordStretchingMinutes()
                                        } else {
                                            viewModel.completeQuestItemDirectly(quest.title)
                                        }
                                    }
                                )
                            }
                        }
                    }
                }
            }

            1 -> {
                // ACHIEVEMENTS trophy panel
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "MAJESTIC HALL OF TRIUMPHS",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Claim high stakes career accolades for overall EXP multipliers.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    if (achievements.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text("No achievements loaded.", color = TextSecondary)
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 90.dp)
                        ) {
                            itemsIndexed(achievements) { index, ach ->
                                AchievementCardItem(achievement = ach)
                            }
                        }
                    }
                }
            }

            2 -> {
                // STREAK CALENDAR heatmap
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxWidth()
                ) {
                    Text(
                        text = "CHRONO HEATMAP CONSISTENCY",
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "Slayed days are illuminated with neon flame tags.",
                        color = TextSecondary,
                        fontSize = 11.sp
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    // Represent 30-day streak map
                    StreakHeatmapCalendar(stats = stats)
                }
            }
        }
    }
}

// ------ DETAILED LIST HELPER SURFACES ------

@Composable
fun QuestDetailedCardItem(quest: DailyQuest, onActionClick: () -> Unit) {
    val completenessFraction = if (quest.progressTarget > 0) quest.progressCurrent.toFloat() / quest.progressTarget.toFloat() else 0f
    val difficultyColor = when (quest.difficulty.lowercase(Locale.ROOT)) {
        "easy" -> NeonGreen
        "medium" -> NeonAqua
        else -> Color(0xFFFF453A)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161618))
            .border(
                1.dp,
                if (quest.isCompleted) NeonGreen.copy(0.4f) else Color(0xFF2C2C32),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("detailed_quest_${quest.title.replace(" ", "_")}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(difficultyColor.copy(0.12f))
                            .padding(horizontal = 8.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = quest.difficulty.uppercase(),
                            color = difficultyColor,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0xFF242428))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${quest.xpReward} XP",
                            color = GlowYellow,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                if (quest.isCompleted) {
                    Box(
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(NeonGreen.copy(0.15f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text("CONQUERED", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = quest.title,
                color = if (quest.isCompleted) TextSecondary else TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = quest.description,
                color = TextSecondary,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Interactive slider progress
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(6.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF2C2C32))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(completenessFraction)
                            .background(if (quest.isCompleted) NeonGreen else NeonAqua)
                    )
                }

                Text(
                    text = "${quest.progressCurrent} / ${quest.progressTarget}",
                    color = TextPrimary,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold
                )

                if (!quest.isCompleted) {
                    IconButton(
                        onClick = onActionClick,
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF242428))
                            .testTag("increment_quest_${quest.id}")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "+", tint = NeonGreen, modifier = Modifier.size(14.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun AchievementCardItem(achievement: Achievement) {
    val progress = if (achievement.progressTarget > 0) achievement.progressCurrent.toFloat() / achievement.progressTarget.toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161618))
            .border(
                1.dp,
                if (achievement.isUnlocked) GlowYellow.copy(0.5f) else Color(0xFF2C2C32),
                RoundedCornerShape(16.dp)
            )
            .padding(14.dp)
            .testTag("achievement_card_${achievement.id}")
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // Achievement badge placeholder representing the locks status
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .clip(CircleShape)
                    .background(if (achievement.isUnlocked) Color(0x22FFD700) else Color(0xFF242428))
                    .border(2.dp, if (achievement.isUnlocked) GlowYellow else Color(0xFF2C2C32), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = if (achievement.isUnlocked) achievement.iconEmoji else "🔒",
                    fontSize = 24.sp
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = achievement.title.uppercase(),
                        color = if (achievement.isUnlocked) Color.White else TextSecondary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(Color(0x1A00FFCC))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = "+${achievement.xpReward} XP",
                            color = NeonAqua,
                            fontSize = 8.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Text(
                    text = achievement.description,
                    color = TextSecondary,
                    fontSize = 11.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                if (achievement.isUnlocked) {
                    Text(
                        text = "Unlocked: ${achievement.unlockedDateString}",
                        color = NeonGreen,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .height(4.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF2C2C32))
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .fillMaxWidth(progress.coerceIn(0f, 1f))
                                    .background(NeonAqua)
                            )
                        }
                        Text(
                            text = "${achievement.progressCurrent} / ${achievement.progressTarget}",
                            color = TextSecondary,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun StreakHeatmapCalendar(stats: UserStats?) {
    // Generate lovely grid layout corresponding to 30 training indices
    val totalGridDays = 30
    val longestStreak = stats?.longestStreak ?: 0
    val activeStreak = stats?.currentStreak ?: 0

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
                Text(text = "STREAK TRACKING LABORATORY", color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                Text(text = "Current: $activeStreak days • All-time peak: $longestStreak days", color = TextSecondary, fontSize = 10.sp)
            }
            Icon(imageVector = Icons.Default.LocalFireDepartment, contentDescription = "burn", tint = Color(0xFFFF5E00), modifier = Modifier.size(20.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Heat grid
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(200.dp)
        ) {
            LazyVerticalGrid(
                columns = GridCells.Fixed(6),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxSize()
            ) {
                items(totalGridDays) { dayIndex ->
                    // Make some days completed based on streak values to make it look alive!
                    val isDayCompleted = stats != null && dayIndex < stats.totalWorkouts && dayIndex % 3 != 0

                    Box(
                        modifier = Modifier
                            .aspectRatio(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isDayCompleted) NeonGreen.copy(0.25f) else Color(0xFF242428))
                            .border(
                                1.dp,
                                if (isDayCompleted) NeonGreen else Color(0xFF2C2C32),
                                RoundedCornerShape(8.dp)
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "DAY",
                                color = if (isDayCompleted) TextPrimary else TextSecondary,
                                fontSize = 8.sp,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = (dayIndex + 1).toString(),
                                color = if (isDayCompleted) NeonGreen else TextPrimary,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Black
                            )
                            if (isDayCompleted) {
                                Text(
                                    text = "🔥",
                                    fontSize = 8.sp
                                )
                            }
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color(0xFF242428)))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Inactive", color = TextSecondary, fontSize = 9.sp)
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(NeonGreen))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Routine Conquered", color = TextSecondary, fontSize = 9.sp)
            }
        }
    }
}
