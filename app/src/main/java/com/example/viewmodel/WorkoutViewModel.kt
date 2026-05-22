package com.example.viewmodel

import android.app.Application
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Handler
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.model.Achievement
import com.example.data.model.DailyQuest
import com.example.data.model.Exercise
import com.example.data.model.UserStats
import com.example.data.model.WorkoutHistory
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutSet
import com.example.data.repository.WorkoutRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.Timer
import java.util.TimerTask

class WorkoutViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = WorkoutRepository(application)

    // Database flows
    val userStats: StateFlow<UserStats?> = repository.userStatsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = null
    )

    val routines: StateFlow<List<WorkoutRoutine>> = repository.routinesFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val history: StateFlow<List<WorkoutHistory>> = repository.historyFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val quests: StateFlow<List<DailyQuest>> = repository.questsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    val achievements: StateFlow<List<Achievement>> = repository.achievementsFlow.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    // --- Active Workout Space state ---
    private val _activeRoutine = MutableStateFlow<WorkoutRoutine?>(null)
    val activeRoutine = _activeRoutine.asStateFlow()

    private val _activeExercises = MutableStateFlow<List<Exercise>>(emptyList())
    val activeExercises = _activeExercises.asStateFlow()

    private val _workoutTimerSeconds = MutableStateFlow(0)
    val workoutTimerSeconds = _workoutTimerSeconds.asStateFlow()

    private val _restTimeTotal = MutableStateFlow(60)
    private val _restTimeRemaining = MutableStateFlow(0)
    val restTimeTotal = _restTimeTotal.asStateFlow()
    val restTimeRemaining = _restTimeRemaining.asStateFlow()
    val isRestActive = _restTimeRemaining.map { it > 0 }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(), false)

    // --- Cinematic Level-up State Overlay ---
    private val _levelUpEvent = MutableStateFlow<Pair<Int, Int>?>(null)
    val levelUpEvent = _levelUpEvent.asStateFlow()

    // Timer jobs
    private var workoutTimerJob: Job? = null
    private var restTimerJob: Job? = null

    init {
        // Initialize DB and auto-seed default entries
        viewModelScope.launch {
            repository.initDatabase()
        }

        // Listen for database level-up notifications to play chimes and trigger cinematic rewards
        repository.onLevelUpCallback = { oldLvl, newLvl ->
            _levelUpEvent.value = Pair(oldLvl, newLvl)
            playLevelUpSoundSync()
        }
    }

    // --- USER CONTROLS ---

    fun updateProfile(avatarName: String, nickname: String) {
        viewModelScope.launch {
            repository.updateAvatar(avatarName, nickname)
        }
    }

    fun recordManualWeightLog(weight: Float) {
        viewModelScope.launch {
            repository.recordManualWeightLog(weight)
        }
    }

    fun completeQuestItemDirectly(questTitle: String) {
        viewModelScope.launch {
            repository.incrementDailyQuestProgress(questTitle, 1)
        }
    }

    fun recordHydrationWater() {
        viewModelScope.launch {
            repository.incrementDailyQuestProgress("Warrior's Shield", 1)
        }
    }

    fun recordStretchingMinutes() {
        viewModelScope.launch {
            repository.incrementDailyQuestProgress("Mental Fortitude", 5)
        }
    }

    fun closeLevelUpCinematic() {
        _levelUpEvent.value = null
    }

    // --- ACTIVE TRACKER WORKFLOWS ---

    fun startWorkoutRoutine(routine: WorkoutRoutine) {
        _activeRoutine.value = routine
        // Parse exercies into initialized sets list
        val exercises = com.example.data.model.JsonUtils.jsonToExercises(routine.exercisesJson)
        // Ensure each exercise has a set list pre-populated matching the targets
        val preppedExercises = exercises.map { ex ->
            val setList = (1..ex.targetSets).map { sn ->
                // Default targets
                WorkoutSet(
                    setNumber = sn,
                    repsCount = ex.targetReps,
                    weightKg = if (routine.category == "Strength") 40f else 0f,
                    isCompleted = false
                )
            }
            ex.copy(sets = setList, exercisesDone = 0)
        }
        _activeExercises.value = preppedExercises
        _workoutTimerSeconds.value = 0
        _restTimeRemaining.value = 0

        // Start timer
        workoutTimerJob?.cancel()
        workoutTimerJob = viewModelScope.launch {
            while (true) {
                delay(1000)
                _workoutTimerSeconds.value += 1
            }
        }
    }

    fun cancelActiveWorkout() {
        workoutTimerJob?.cancel()
        restTimerJob?.cancel()
        _activeRoutine.value = null
        _activeExercises.value = emptyList()
        _workoutTimerSeconds.value = 0
        _restTimeRemaining.value = 0
    }

    fun toggleSetCompletion(exerciseIndex: Int, setIndex: Int) {
        val currentList = _activeExercises.value.toMutableList()
        if (exerciseIndex >= currentList.size) return
        val ex = currentList[exerciseIndex]
        val setList = ex.sets.toMutableList()
        if (setIndex >= setList.size) return
        
        val set = setList[setIndex]
        val nextCompleted = !set.isCompleted
        setList[setIndex] = set.copy(isCompleted = nextCompleted)
        
        // Count total completed sets
        val completedCount = setList.count { it.isCompleted }
        
        currentList[exerciseIndex] = ex.copy(sets = setList, exercisesDone = completedCount)
        _activeExercises.value = currentList

        // Trigger rest clock on successfully ticking off a set
        if (nextCompleted) {
            triggerRestTimer(ex.restSeconds)
            // Play a light audio pop sound for tap satisfaction
            playSetCompletionClick()
        }
    }

    fun updateSetValues(exerciseIndex: Int, setIndex: Int, weight: Float, reps: Int) {
        val currentList = _activeExercises.value.toMutableList()
        if (exerciseIndex >= currentList.size) return
        val ex = currentList[exerciseIndex]
        val setList = ex.sets.toMutableList()
        if (setIndex >= setList.size) return

        val set = setList[setIndex]
        setList[setIndex] = set.copy(weightKg = weight, repsCount = reps)
        currentList[exerciseIndex] = ex.copy(sets = setList)
        _activeExercises.value = currentList
    }

    fun finishActiveWorkout() {
        val routine = _activeRoutine.value ?: return
        val durationMins = maxOf(1, _workoutTimerSeconds.value / 60)
        
        // Count how many sets actually finished to compute some accuracy modifier!
        val exercises = _activeExercises.value
        val totalExpected = exercises.flatMap { it.sets }.size
        val totalCompleted = exercises.flatMap { it.sets }.count { it.isCompleted }
        
        // Base modifiers
        val activeExp = routine.expReward
        val activeCalories = durationMins * 8 // roughly 8 kcal/min

        viewModelScope.launch {
            repository.completeWorkout(
                routineName = routine.name,
                category = routine.category,
                durationMinutes = durationMins,
                caloriesBurned = activeCalories,
                expGained = activeExp,
                exercises = exercises
            )
            cancelActiveWorkout()
        }
    }

    fun createCustomRoutine(name: String, desc: String, category: String, duration: Int, exp: Int, exercises: List<Exercise>) {
        viewModelScope.launch {
            repository.createCustomRoutine(name, desc, category, duration, exp, exercises)
        }
    }

    fun deleteRoutine(routine: WorkoutRoutine) {
        viewModelScope.launch {
            repository.deleteRoutine(routine)
        }
    }

    private fun triggerRestTimer(seconds: Int) {
        _restTimeTotal.value = seconds
        _restTimeRemaining.value = seconds

        restTimerJob?.cancel()
        restTimerJob = viewModelScope.launch {
            while (_restTimeRemaining.value > 0) {
                delay(1000)
                _restTimeRemaining.value -= 1
            }
        }
    }

    fun skipRestTimer() {
        _restTimeRemaining.value = 0
        restTimerJob?.cancel()
    }

    // --- AUDIO GENERATION / DIGITAL SYNTH SYSTEM ---

    private fun playLevelUpSoundSync() {
        try {
            // RPG fan-fare level up sound built natively from tone synthesizer!
            val toneG = ToneGenerator(AudioManager.STREAM_NOTIFICATION, 95)
            // Start simple high chime sequence
            Thread {
                toneG.startTone(ToneGenerator.TONE_PROP_BEEP)
                Thread.sleep(150)
                toneG.startTone(ToneGenerator.TONE_CDMA_PIP, 200)
                Thread.sleep(200)
                toneG.startTone(ToneGenerator.TONE_CDMA_HIGH_L, 300)
            }.start()
        } catch (e: Exception) {
            // Ignore sound error gracefully
        }
    }

    private fun playSetCompletionClick() {
        try {
            val toneG = ToneGenerator(AudioManager.STREAM_SYSTEM, 60)
            toneG.startTone(ToneGenerator.TONE_PROP_ACK, 80)
        } catch (e: Exception) {
            // Ignore sound errors
        }
    }
}
