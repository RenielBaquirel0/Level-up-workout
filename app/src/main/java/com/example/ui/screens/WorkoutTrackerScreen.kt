package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassTop
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.Exercise
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSet
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkoutTrackerScreen(
    viewModel: WorkoutViewModel,
    modifier: Modifier = Modifier
) {
    val routines by viewModel.routines.collectAsState()
    val activeRoutine by viewModel.activeRoutine.collectAsState()
    val activeExercises by viewModel.activeExercises.collectAsState()
    val timerSeconds by viewModel.workoutTimerSeconds.collectAsState()

    // Screen State
    var selectedRoutineForDetails by remember { mutableStateOf<WorkoutRoutine?>(null) }
    var showCustomRoutineCreator by remember { mutableStateOf(false) }

    // Dialog state for modifying reps/weights on standard sets
    var editingSetIndices by remember { mutableStateOf<Triple<Int, Int, WorkoutSet>?>(null) } // exerciseIndex, setIndex, WorkoutSet

    Box(modifier = modifier.fillMaxSize().background(Color(0xFF0D0D0D))) {
        if (activeRoutine != null) {
            // --- ACTIVE WORKOUT SESSION TRACKER WINDOW ---
            ActiveWorkoutSessionView(
                routine = activeRoutine!!,
                exercises = activeExercises,
                timerSeconds = timerSeconds,
                viewModel = viewModel,
                onEditSet = { exIdx, setIdx, set ->
                    editingSetIndices = Triple(exIdx, setIdx, set)
                }
            )
        } else {
            // --- MAIN SELECTION DASHBOARD ---
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                Spacer(modifier = Modifier.height(16.dp))
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = "TRAINING PROTOCOLS",
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 18.sp,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Slay routines to farm character stats",
                            color = TextSecondary,
                            fontSize = 11.sp
                        )
                    }

                    Button(
                        onClick = { showCustomRoutineCreator = true },
                        colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                        modifier = Modifier
                            .height(36.dp)
                            .testTag("create_routine_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = "Add", modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("CREATE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (routines.isEmpty()) {
                    Box(modifier = Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                        Text("No training modules found.", color = TextSecondary)
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 90.dp)
                    ) {
                        itemsIndexed(routines) { index, routine ->
                            RoutineCardItem(
                                routine = routine,
                                onClick = { selectedRoutineForDetails = routine },
                                onDelete = { viewModel.deleteRoutine(routine) }
                            )
                        }
                    }
                }
            }
        }

        // --- EXERCISE DETAILS BOTTOM SHEET / POPUP ---
        selectedRoutineForDetails?.let { routine ->
            ExerciseDetailsDialog(
                routine = routine,
                onDismiss = { selectedRoutineForDetails = null },
                onBegin = {
                    viewModel.startWorkoutRoutine(routine)
                    selectedRoutineForDetails = null
                }
            )
        }

        // --- CUSTOM PROTOCOL BUILDER DIALOG ---
        if (showCustomRoutineCreator) {
            CustomRoutineCreatorDialog(
                onDismiss = { showCustomRoutineCreator = false },
                onSave = { name, desc, cat, dur, exp, exercises ->
                    viewModel.createCustomRoutine(name, desc, cat, dur, exp, exercises)
                    showCustomRoutineCreator = false
                }
            )
        }

        // --- SET REPS & WEIGHT VALUE MODIFIER POPUP ---
        editingSetIndices?.let { triple ->
            EditSetSelectorDialog(
                exerciseIndex = triple.first,
                setIndex = triple.second,
                initialSet = triple.third,
                onDismiss = { editingSetIndices = null },
                onSave = { reps, weight ->
                    viewModel.updateSetValues(triple.first, triple.second, weight, reps)
                    editingSetIndices = null
                }
            )
        }
    }
}

// ------ ROBUST REUSABLE VIEWS ------

@Composable
fun RoutineCardItem(
    routine: WorkoutRoutine,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val categoryGlow = when (routine.category) {
        "Strength" -> NeonGreen
        "Endurance" -> NeonAqua
        "Agility" -> Color(0xFFBF00FF)
        else -> GlowYellow
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF161618))
            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(16.dp))
            .clickable { onClick() }
            .padding(16.dp)
            .testTag("routine_card_${routine.name.replace(" ", "_")}")
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(categoryGlow.copy(alpha = 0.15f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = routine.category.uppercase(),
                        color = categoryGlow,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )
                }

                if (routine.isCustom) {
                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier.size(24.dp).testTag("delete_routine_${routine.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Delete Routine",
                            tint = Color.Red.copy(0.7f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = routine.name,
                color = TextPrimary,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = routine.description,
                color = TextSecondary,
                fontSize = 11.sp,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.Timer, contentDescription = "Time", tint = TextSecondary, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "~${routine.durationMinutesEstimate} MINS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(imageVector = Icons.Default.HourglassTop, contentDescription = "EXP", tint = NeonGreen, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "+${routine.expReward} EXP", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Black)
                }
            }
        }
    }
}

@Composable
fun ExerciseDetailsDialog(
    routine: WorkoutRoutine,
    onDismiss: () -> Unit,
    onBegin: () -> Unit
) {
    val exercises = remember(routine) { com.example.data.model.JsonUtils.jsonToExercises(routine.exercisesJson) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Column {
                Text(
                    text = routine.name.uppercase(),
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
                Text(
                    text = "EXP REWARD: +${routine.expReward} POINTS",
                    color = NeonGreen,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 11.sp,
                    letterSpacing = 1.sp
                )
            }
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                Text(
                    text = routine.description,
                    color = TextSecondary,
                    fontSize = 12.sp,
                )
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = "TARGET EXERCISE MATRIX",
                    color = TextPrimary,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Spacer(modifier = Modifier.height(6.dp))

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(max = 240.dp)
                ) {
                    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        itemsIndexed(exercises) { idx, ex ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color(0xFF242428))
                                    .padding(10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(ex.name, color = TextPrimary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                                    Text("Sets rest interval: ${ex.restSeconds} seconds", color = TextSecondary, fontSize = 9.sp)
                                }
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(Color(0xFF2C2C32))
                                        .padding(horizontal = 8.dp, vertical = 4.dp)
                                ) {
                                    Text(
                                        text = "${ex.targetSets} sets × ${ex.targetReps} reps",
                                        color = NeonGreen,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onBegin,
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("dialog_begin_workout")
            ) {
                Text("BEGIN PROTOCOL", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF161618)
    )
}

@Composable
fun ActiveWorkoutSessionView(
    routine: WorkoutRoutine,
    exercises: List<Exercise>,
    timerSeconds: Int,
    viewModel: WorkoutViewModel,
    onEditSet: (Int, Int, WorkoutSet) -> Unit
) {
    val restRemaining by viewModel.restTimeRemaining.collectAsState()
    val isRestActive by viewModel.isRestActive.collectAsState()
    val restTotal by viewModel.restTimeTotal.collectAsState()

    val formattedTime = remember(timerSeconds) {
        val mins = timerSeconds / 60
        val secs = timerSeconds % 60
        String.format(Locale.ROOT, "%02d:%02d", mins, secs)
    }

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D))
                    .padding(16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "ACTIVE SECURE EXPEDITION",
                        color = NeonGreen,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.5.sp
                    )
                    Text(
                        text = routine.name,
                        color = TextPrimary,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Black,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.width(180.dp)
                    )
                }

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color(0xFF161618))
                            .border(1.dp, Color(0xFF2C2C32), RoundedCornerShape(8.dp))
                            .padding(horizontal = 8.dp, vertical = 4.dp)
                    ) {
                        Text(
                            text = formattedTime,
                            color = TextPrimary,
                            fontWeight = FontWeight.Black,
                            fontSize = 14.sp
                        )
                    }

                    IconButton(
                        onClick = { viewModel.cancelActiveWorkout() },
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(Color.Red.copy(0.15f))
                            .testTag("cancel_workout_button")
                    ) {
                        Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = Color.Red, modifier = Modifier.size(16.dp))
                    }
                }
            }
        },
        bottomBar = {
            // Persistent bottom trigger
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0D0D0D))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Button(
                    onClick = { viewModel.finishActiveWorkout() },
                    colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .testTag("finish_workout_button"),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(imageVector = Icons.Default.Check, contentDescription = "Save")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("COMPLETE PROTOCOL & CLAIM EXP", fontWeight = FontWeight.Black, letterSpacing = 0.5.sp)
                }
            }
        },
        containerColor = Color(0xFF0D0D0D)
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
                contentPadding = PaddingValues(bottom = 120.dp)
            ) {
                itemsIndexed(exercises) { exerciseIndex, exercise ->
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
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = exercise.name,
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                                Text(
                                    text = "Rest intervals: ${exercise.restSeconds}s",
                                    color = TextSecondary,
                                    fontSize = 10.sp
                                )
                            }
                            Text(
                                text = "Sets finished: ${exercise.exercisesDone}/${exercise.targetSets}",
                                color = NeonGreen,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        // Header titles for columns
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text("SET", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f))
                            Text("REPS", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text("WEIGHT KG", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1.5f), textAlign = TextAlign.Center)
                            Text("CONQUER", color = TextSecondary, fontSize = 10.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = TextAlign.End)
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        exercise.sets.forEachIndexed { setIndex, set ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (set.isCompleted) Color(0x1100FF66) else Color.Transparent)
                                    .clickable { onEditSet(exerciseIndex, setIndex, set) }
                                    .padding(vertical = 4.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                // Set number
                                Text(
                                    text = "0${set.setNumber}",
                                    color = if (set.isCompleted) NeonGreen else TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 12.sp,
                                    modifier = Modifier.weight(1f)
                                )

                                // Reps
                                Text(
                                    text = set.repsCount.toString(),
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.Center
                                )

                                // Weight
                                Text(
                                    text = "${set.weightKg} kg",
                                    color = TextPrimary,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    modifier = Modifier.weight(1.5f),
                                    textAlign = TextAlign.Center
                                )

                                // Check checkbox
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight(),
                                    contentAlignment = Alignment.CenterEnd
                                ) {
                                    IconButton(
                                        onClick = { viewModel.toggleSetCompletion(exerciseIndex, setIndex) },
                                        modifier = Modifier
                                            .size(28.dp)
                                            .clip(CircleShape)
                                            .background(if (set.isCompleted) NeonGreen else Color(0xFF2C2C32))
                                            .testTag("toggle_set_${exerciseIndex}_$setIndex")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = "Conquer Set",
                                            tint = if (set.isCompleted) Color.Black else TextSecondary,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            // --- FROSTED / NEON FLOATING REST TIMER OVERLAY ---
            AnimatedVisibility(
                visible = isRestActive,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 2 }),
                exit = fadeOut() + slideOutVertically(targetOffsetY = { it / 2 }),
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 16.dp, start = 16.dp, end = 16.dp)
            ) {
                RestTimerOverlayCard(
                    timeRemaining = restRemaining,
                    timeTotal = restTotal,
                    onSkip = { viewModel.skipRestTimer() }
                )
            }
        }
    }
}

@Composable
fun RestTimerOverlayCard(timeRemaining: Int, timeTotal: Int, onSkip: () -> Unit) {
    val progress = if (timeTotal > 0) timeRemaining.toFloat() / timeTotal.toFloat() else 0f

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(Color(0xE6161618)) // frosted glass look with transparency
            .border(1.dp, NeonAqua.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier.size(40.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(
                        progress = progress,
                        color = NeonAqua,
                        trackColor = Color(0xFF2C2C32),
                        strokeWidth = 3.dp,
                        modifier = Modifier.fillMaxSize()
                    )
                    Icon(
                        imageVector = Icons.Default.HourglassBottom,
                        contentDescription = "Resting",
                        tint = NeonAqua,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(text = "REST PROTOCOL ACTIVE", color = NeonAqua, fontSize = 10.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.sp)
                    Text(text = "Take breath: $timeRemaining seconds left", color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }

            Button(
                onClick = onSkip,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C32), contentColor = TextPrimary),
                shape = RoundedCornerShape(8.dp),
                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                modifier = Modifier
                    .height(32.dp)
                    .testTag("skip_rest_button")
            ) {
                Text("SKIP", fontSize = 10.sp, fontWeight = FontWeight.Bold)
            }
        }
    }
}

// Dialog for editing reps/weight directly
@Composable
fun EditSetSelectorDialog(
    exerciseIndex: Int,
    setIndex: Int,
    initialSet: WorkoutSet,
    onDismiss: () -> Unit,
    onSave: (Int, Float) -> Unit
) {
    var repsText by remember { mutableStateOf(initialSet.repsCount.toString()) }
    var weightText by remember { mutableStateOf(initialSet.weightKg.toString()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("SET REPETITION INTERFACE", color = TextPrimary, fontSize = 14.sp, fontWeight = FontWeight.Bold)
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = repsText,
                    onValueChange = { repsText = it },
                    label = { Text("REPETITIONS (Reps)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color(0xFF2C2C32),
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_reps_input")
                )

                OutlinedTextField(
                    value = weightText,
                    onValueChange = { weightText = it },
                    label = { Text("LOAD WEIGHT (Kg)", color = TextSecondary) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonGreen,
                        unfocusedBorderColor = Color(0xFF2C2C32),
                        focusedLabelColor = NeonGreen
                    ),
                    modifier = Modifier.fillMaxWidth().testTag("edit_weight_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val reps = repsText.toIntOrNull() ?: initialSet.repsCount
                    val weight = weightText.toFloatOrNull() ?: initialSet.weightKg
                    onSave(reps, weight)
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_set_edit")
            ) {
                Text("SAVE VALUE", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("CANCEL", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF161618)
    )
}

// Create custom protocols dialog layout
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomRoutineCreatorDialog(
    onDismiss: () -> Unit,
    onSave: (String, String, String, Int, Int, List<Exercise>) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var category by remember { mutableStateOf("Strength") }
    var durationMins by remember { mutableStateOf("30") }

    // Exercises
    var tempExName by remember { mutableStateOf("") }
    var tempExSets by remember { mutableStateOf("3") }
    var tempExReps by remember { mutableStateOf("10") }
    val customExercisesList = remember { mutableStateListOf<Exercise>() }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("CREATE SYSTEM PROTOCOL", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp) },
        text = {
            Box(modifier = Modifier.sizeIn(maxHeight = 400.dp)) {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    item {
                        OutlinedTextField(
                            value = name,
                            onValueChange = { name = it },
                            label = { Text("PROTOCOL DESIGNATION", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth().testTag("routine_name_input")
                        )
                    }
                    item {
                        OutlinedTextField(
                            value = desc,
                            onValueChange = { desc = it },
                            label = { Text("INTELLIGENCE BRIEFING / DESC", color = TextSecondary) },
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        Text("RPG CLASS CATEGORY", color = TextSecondary, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Strength", "Endurance", "Agility", "Discipline").forEach { cat ->
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(if (category == cat) NeonGreen else Color(0xFF242428))
                                        .clickable { category = cat }
                                        .padding(horizontal = 10.dp, vertical = 6.dp)
                                ) {
                                    Text(
                                        text = cat.uppercase(),
                                        color = if (category == cat) Color.Black else TextPrimary,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }
                    item {
                        OutlinedTextField(
                            value = durationMins,
                            onValueChange = { durationMins = it },
                            label = { Text("ESTIMATED MINUTES", color = TextSecondary) },
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            colors = OutlinedTextFieldDefaults.colors(focusedBorderColor = NeonGreen),
                            modifier = Modifier.fillMaxWidth()
                        )
                    }

                    // Exercises Addition Section
                    item {
                        Divider(color = Color(0xFF2C2C32))
                        Spacer(modifier = Modifier.height(6.dp))
                        Text("APPEND DRILLS / EXERCISES", color = NeonAqua, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    }

                    item {
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            OutlinedTextField(
                                value = tempExName,
                                onValueChange = { tempExName = it },
                                label = { Text("Exercise Name", color = TextSecondary) },
                                modifier = Modifier.fillMaxWidth().testTag("exercise_name_input")
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedTextField(
                                    value = tempExSets,
                                    onValueChange = { tempExSets = it },
                                    label = { Text("Sets", color = TextSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("exercise_sets_input")
                                )
                                OutlinedTextField(
                                    value = tempExReps,
                                    onValueChange = { tempExReps = it },
                                    label = { Text("Reps", color = TextSecondary) },
                                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                    modifier = Modifier.weight(1f).testTag("exercise_reps_input")
                                )
                            }

                            Button(
                                onClick = {
                                    if (tempExName.isNotBlank()) {
                                        val setsInt = tempExSets.toIntOrNull() ?: 3
                                        val repsInt = tempExReps.toIntOrNull() ?: 10
                                        customExercisesList.add(
                                            Exercise(
                                                name = tempExName,
                                                targetSets = setsInt,
                                                targetReps = repsInt,
                                                restSeconds = 60,
                                                sets = emptyList()
                                            )
                                        )
                                        tempExName = ""
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2C2C32), contentColor = TextPrimary),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.fillMaxWidth().testTag("add_exercise_drill")
                            ) {
                                Text("ADD EXERCISE", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                            }
                        }
                    }

                    // Added exercise list
                    itemsIndexed(customExercisesList) { idx, ex ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(0xFF242428))
                                .padding(8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(ex.name, color = TextPrimary, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, modifier = Modifier.weight(1f))
                            Text("${ex.targetSets}s x ${ex.targetReps}r", color = NeonGreen, fontSize = 10.sp, fontWeight = FontWeight.Bold)
                            IconButton(onClick = { customExercisesList.removeAt(idx) }, modifier = Modifier.size(24.dp)) {
                                Icon(imageVector = Icons.Default.Close, contentDescription = "Delete", tint = Color.Red, modifier = Modifier.size(14.dp))
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && customExercisesList.isNotEmpty()) {
                        val duration = durationMins.toIntOrNull() ?: 30
                        // Reward exp estimate: duration * 1.5 rounded
                        val exp = (duration * 1.5f).toInt()
                        onSave(name, desc, category, duration, exp, customExercisesList.toList())
                    }
                },
                colors = ButtonDefaults.buttonColors(containerColor = NeonGreen, contentColor = Color.Black),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.testTag("save_custom_routine")
            ) {
                Text("DEPLOY PROTOCOL", fontWeight = FontWeight.Black)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("ABORT", color = TextSecondary)
            }
        },
        containerColor = Color(0xFF161618)
    )
}
