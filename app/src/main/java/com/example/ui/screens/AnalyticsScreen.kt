package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.MonitorWeight
import androidx.compose.material.icons.filled.ShowChart
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.JsonUtils
import com.example.data.model.UserStats
import com.example.data.model.WorkoutHistory
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
fun AnalyticsScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val stats by viewModel.userStats.collectAsState()
    val historyList by viewModel.history.collectAsState()

    var showWeightDialog by remember { mutableStateOf(false) }
    var currentWeightInput by remember { mutableStateOf("75.0") }
    var expandedHistoryId by remember { mutableStateOf<Int?>(null) }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF0D0D0D))
            .padding(horizontal = 16.dp),
        contentPadding = PaddingValues(top = 16.dp, bottom = 90.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. GENERAL STATS & KILOCALORIES BANNER ---
        item {
            Column {
                Text(
                    text = "PROGRESS INTELLIGENCE",
                    color = TextPrimary,
                    fontWeight = FontWeight.Black,
                    fontSize = 18.sp,
                    letterSpacing = 1.sp
                )
                Text(
                    text = "Combat summaries & telemetry charts",
                    color = TextSecondary,
                    fontSize = 11.sp
                )
            }
        }

        item {
            stats?.let { uStats ->
                CareerSummaryGrid(stats = uStats, onLogWeightClick = { showWeightDialog = true })
            }
        }

        // --- 2. CANVAS ANALYTICS CHART LIST ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "XP PROGRESS CHRONOLOGY",
                            color = TextPrimary,
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp,
                            letterSpacing = 1.sp
                        )
                        Icon(imageVector = Icons.Default.ShowChart, contentDescription = "trend", tint = NeonGreen, modifier = Modifier.size(16.dp))
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Dynamic canvas-based graph using history xp info
                    XPTrendsGraph(history = historyList)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Visualizes accumulated XP gains over historical workout campaigns.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- 3. DOCK WEEKLY CAMPAIGNS HISTOGRAM ---
        item {
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp)),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "WEEKLY FREQUENCY INTENSITY",
                        color = TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp,
                        letterSpacing = 1.sp
                    )
                    Spacer(modifier = Modifier.height(16.dp))

                    WeeklyFrequencyGraph(history = historyList)

                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "Bar chart of workout sessions logged across current week cycle.",
                        color = TextSecondary,
                        fontSize = 10.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }

        // --- 4. HISTORY LOG ENTRY ROOM ---
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(imageVector = Icons.Default.History, contentDescription = "history", tint = NeonAqua, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CAMPAIGN HISTORY LOGS",
                    color = TextPrimary,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
            }
        }

        if (historyList.isEmpty()) {
            item {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(Color(0xFF161618)),
                    contentAlignment = Alignment.Center
                ) {
                    Text(text = "🛡️ SECURE YOUR FIRST COMPLETED PROTOCOL TO SEED HISTORIES", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
                }
            }
        } else {
            itemsIndexed(historyList) { index, log ->
                val isExpanded = expandedHistoryId == log.id
                WorkoutHistoryCard(
                    log = log,
                    isExpanded = isExpanded,
                    onToggleExpand = {
                        expandedHistoryId = if (isExpanded) null else log.id
                    }
                )
            }
        }
    }

    // Weight registration dialog popup
    if (showWeightDialog) {
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = {
                Text(
                    text = "TELEMETRY WEIGHT INSCRIPTIONS",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp
                )
            },
            text = {
                OutlinedTextField(
                    value = currentWeightInput,
                    onValueChange = { currentWeightInput = it },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    label = { Text("BODY MASS (KG)", color = TextSecondary) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color(0xFF2C2C32),
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("weight_tracker_input")
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val wt = currentWeightInput.toFloatOrNull() ?: 75f
                        viewModel.recordManualWeightLog(wt)
                        showWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("save_weight_button")
                ) {
                    Text(text = "LOG VALUE", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text(text = "ABORT", color = TextSecondary)
                }
            },
            containerColor = Color(0xFF161618)
        )
    }
}

// ------ SPECIALIZED ANALYSTS FOR DISPLAY ------

@Composable
fun CareerSummaryGrid(stats: UserStats, onLogWeightClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricsDisplayCard(
                title = "WORKOUTS",
                value = stats.totalWorkouts.toString(),
                subtitle = "CAMPAIGNS",
                color = NeonGreen
            )

            // Dynamic interaction weight log card
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onLogWeightClick() }
                    .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp))
                    .testTag("body_weight_stat_card"),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(text = "BODY MASS", color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "75.0 KG", color = NeonAqua, fontSize = 20.sp, fontWeight = FontWeight.Black)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(text = "⚡ TAP TO INSCRIP", color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
                }
            }
        }

        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            MetricsDisplayCard(
                title = "kcal BURNT",
                value = stats.totalCalories.toString(),
                subtitle = "TOTAL ACTIVE ENERGY",
                color = Color(0xFFFF5E00)
            )

            MetricsDisplayCard(
                title = "XP EARNED",
                value = stats.totalXpEarned.toString(),
                subtitle = "RPG SCORE",
                color = GlowYellow
            )
        }
    }
}

@Composable
fun MetricsDisplayCard(title: String, value: String, subtitle: String, color: Color) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp)),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Text(text = title, color = TextSecondary, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = value, color = color, fontSize = 24.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.height(2.dp))
            Text(text = subtitle, color = TextSecondary, fontSize = 8.sp)
        }
    }
}

// Generates line chart using Canvas drawing directly
@Composable
fun XPTrendsGraph(history: List<WorkoutHistory>) {
    // Generate simple mock points or read history
    val graphPoints = remember(history) {
        val basePoints = mutableListOf<Float>()
        var progressSum = 100f // starts at 100
        basePoints.add(progressSum)
        
        // Take up to 6 latest campaigns in reverse cron (chronological order)
        val sortedHistory = history.take(6).reversed()
        for (item in sortedHistory) {
            progressSum += item.expEarned
            basePoints.add(progressSum)
        }

        // Pad list if too short
        while (basePoints.size < 6) {
            basePoints.add(0, 50f + (basePoints.size * 20))
        }
        basePoints.takeLast(6)
    }

    Canvas(
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
            .background(Color(0xFF242428), RoundedCornerShape(8.dp))
            .padding(top = 16.dp, bottom = 4.dp, start = 12.dp, end = 12.dp)
    ) {
        val width = size.width
        val height = size.height

        val maxVal = graphPoints.maxOrNull() ?: 1000f
        val minVal = graphPoints.minOrNull() ?: 0f
        val delta = if (maxVal == minVal) 1f else maxVal - minVal

        val points = mutableListOf<Offset>()
        val stepX = width / 5f

        for (i in graphPoints.indices) {
            val ratio = (graphPoints[i] - minVal) / delta
            val x = i * stepX
            val y = height - (ratio * height)
            points.add(Offset(x, y))
        }

        // Draw background horizontal lines
        for (grid in 1..3) {
            val lineY = height * (grid / 4f)
            drawLine(
                color = Color(0xFF2C2C32),
                start = Offset(0f, lineY),
                end = Offset(width, lineY),
                strokeWidth = 1f
            )
        }

        // Draw Line Path
        val path = Path().apply {
            if (points.isNotEmpty()) {
                moveTo(points[0].x, points[0].y)
                for (j in 1 until points.size) {
                    lineTo(points[j].x, points[j].y)
                }
            }
        }

        drawPath(
            path = path,
            color = NeonGreen,
            style = Stroke(width = 4f)
        )

        // Draw Dots
        points.forEach { point ->
            drawCircle(
                color = Color.White,
                radius = 5f,
                center = point
            )
            drawCircle(
                color = NeonGreen,
                radius = 8f,
                center = point,
                style = Stroke(width = 3f)
            )
        }
    }
}

// Generates weekly bar charts using Canvas drawing directly
@Composable
fun WeeklyFrequencyGraph(history: List<WorkoutHistory>) {
    // Generate dummy weekly column levels
    val volumes = listOf(0.4f, 0.8f, 0.2f, 0.9f, 0.0f, 0.5f, 0.7f)
    val daysLabels = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(120.dp)
            .background(Color(0xFF242428), RoundedCornerShape(8.dp))
            .padding(12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        volumes.forEachIndexed { i, vol ->
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom,
                modifier = Modifier.weight(1f)
            ) {
                // Bar height
                val heightPercent = vol.coerceIn(0.05f..1f)
                Box(
                    modifier = Modifier
                        .fillMaxHeight(0.85f)
                        .fillMaxWidth(0.35f)
                        .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .fillMaxHeight(heightPercent)
                            .background(if (vol > 0.5f) NeonAqua else NeonGreen)
                    )
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(text = daysLabels[i], color = TextSecondary, fontSize = 8.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun WorkoutHistoryCard(
    log: WorkoutHistory,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp))
            .clickable { onToggleExpand() }
            .testTag("history_log_card_${log.id}"),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF161618))
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = log.routineName.uppercase(), color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Black)
                    Text(text = "${log.dateString} • ~${log.durationMinutes} Mins logged", color = TextSecondary, fontSize = 10.sp)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(Color(0xFF1100FF66).copy(0.12f))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(text = "+${log.expEarned} XP", color = NeonGreen, fontSize = 9.sp, fontWeight = FontWeight.ExtraBold)
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    Icon(
                        imageVector = if (isExpanded) Icons.Default.ArrowDropUp else Icons.Default.ArrowDropDown,
                        contentDescription = "expander",
                        tint = TextSecondary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }

            AnimatedVisibility(visible = isExpanded) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(text = "CONQUERED EXERCISES & DRILL PERFORMANCE", color = NeonAqua, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.5.sp)
                    Spacer(modifier = Modifier.height(4.dp))

                    val exercises = remember(log) { JsonUtils.jsonToExercises(log.exercisesJson) }
                    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        exercises.forEach { ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF242428))
                                    .padding(8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(text = ex.name, color = TextPrimary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                                Text(
                                    text = "${ex.sets.count { it.isCompleted }} / ${ex.targetSets} sets finished",
                                    color = NeonGreen,
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
