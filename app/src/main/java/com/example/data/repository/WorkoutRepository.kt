package com.example.data.repository

import android.content.Context
import com.example.data.AppDatabase
import com.example.data.model.Achievement
import com.example.data.model.DailyQuest
import com.example.data.model.Exercise
import com.example.data.model.JsonUtils
import com.example.data.model.UserStats
import com.example.data.model.WorkoutHistory
import com.example.data.model.WorkoutRoutine
import com.example.data.model.AvatarUtils
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WorkoutRepository(context: Context) {
    private val db = AppDatabase.getDatabase(context)
    private val userStatsDao = db.userStatsDao()
    private val workoutRoutineDao = db.workoutRoutineDao()
    private val workoutHistoryDao = db.workoutHistoryDao()
    private val dailyQuestDao = db.dailyQuestDao()
    private val achievementDao = db.achievementDao()

    // Exposed Flows
    val userStatsFlow: Flow<UserStats?> = userStatsDao.getUserStatsFlow()
    val routinesFlow: Flow<List<WorkoutRoutine>> = workoutRoutineDao.getAllRoutinesFlow()
    val historyFlow: Flow<List<WorkoutHistory>> = workoutHistoryDao.getAllHistoryFlow()
    val questsFlow: Flow<List<DailyQuest>> = dailyQuestDao.getAllQuestsFlow()
    val achievementsFlow: Flow<List<Achievement>> = achievementDao.getAllAchievementsFlow()

    // Level-up triggers for animation feedback
    var onLevelUpCallback: ((oldLevel: Int, newLevel: Int) -> Unit)? = null

    suspend fun initDatabase() {
        // Seed initial stats
        if (userStatsDao.getUserStats() == null) {
            userStatsDao.insertUserStats(UserStats())
        }

        // Seed initial routines
        if (workoutRoutineDao.getCount() == 0) {
            val defaultRoutines = listOf(
                WorkoutRoutine(
                    name = "🛡️ Hypertrophy Push Day",
                    description = "Focus on Chest, Shoulders & Triceps to maximize Upper-body Power.",
                    category = "Strength",
                    durationMinutesEstimate = 45,
                    expReward = 60,
                    exercisesJson = JsonUtils.exercisesToJson(listOf(
                        Exercise("Flat Barbell Bench Press", 10, 4),
                        Exercise("Seated Dumbbell Overhead Press", 12, 3),
                        Exercise("Incline Dumbbell Chest Fly", 15, 3),
                        Exercise("Cable Triceps Pushdown", 12, 4)
                    ))
                ),
                WorkoutRoutine(
                    name = "🏃 Cyber Endurance Flow",
                    description = "Unleash high performance stamina with a brutal leg and core routine.",
                    category = "Endurance",
                    durationMinutesEstimate = 40,
                    expReward = 50,
                    exercisesJson = JsonUtils.exercisesToJson(listOf(
                        Exercise("Barbell Back Squat", 8, 4),
                        Exercise("Romanian Deadlift", 12, 3),
                        Exercise("Leg Extensions", 15, 3),
                        Exercise("Hanging Knee Raises", 20, 3)
                    ))
                ),
                WorkoutRoutine(
                    name = "⚡ HIIT Agility Protocol",
                    description = "Fast-paced intervals to supercharge reflexes and condition your heart.",
                    category = "Agility",
                    durationMinutesEstimate = 25,
                    expReward = 55,
                    exercisesJson = JsonUtils.exercisesToJson(listOf(
                        Exercise("Mountain Climbers", 30, 4),
                        Exercise("Weighted Kettlebell Swings", 15, 3),
                        Exercise("Plyometric Jump Squats", 12, 3),
                        Exercise("Dynamic Jumping Jacks", 50, 4)
                    ))
                ),
                WorkoutRoutine(
                    name = "🔥 Core Discipline Guard",
                    description = "An intense abdominals shield designed to test extreme perseverance.",
                    category = "Discipline",
                    durationMinutesEstimate = 20,
                    expReward = 40,
                    exercisesJson = JsonUtils.exercisesToJson(listOf(
                        Exercise("Plank Hold (Secs)", 60, 3),
                        Exercise("Russian twists (Reps)", 20, 3),
                        Exercise("Bicycle Crunches", 30, 3),
                        Exercise("Kneeling Ab Wheel Rollouts", 12, 3)
                    ))
                ),
                WorkoutRoutine(
                    name = "⚔️ Barbarian Might",
                    description = "Classic compound pull movement setup designed to forge a powerful back.",
                    category = "Strength",
                    durationMinutesEstimate = 45,
                    expReward = 65,
                    exercisesJson = JsonUtils.exercisesToJson(listOf(
                        Exercise("Deadlifts", 5, 5),
                        Exercise("Wide Grip Pullups", 8, 4),
                        Exercise("Bent Over Barbell Rows", 10, 4),
                        Exercise("Seated Facepulls", 15, 3)
                    ))
                )
            )
            for (routine in defaultRoutines) {
                workoutRoutineDao.insertRoutine(routine)
            }
        }

        // Seed initial Daily Quests
        val currentQuests = dailyQuestDao.getAllQuestsFlow().firstOrNull() ?: emptyList()
        val today = getTodayDateString()
        
        // Check if we need to reset/seed daily quests
        val currentStats = userStatsDao.getUserStats()
        if (currentQuests.isEmpty() || currentStats?.lastQuestResetDateString != today) {
            dailyQuestDao.deleteQuests()
            val defaultQuests = listOf(
                DailyQuest(
                    title = "🛡️ Warrior's Shield",
                    description = "Drink 3.0 Liters of pure water.",
                    category = "Endurance",
                    xpReward = 15,
                    progressCurrent = 0,
                    progressTarget = 3,
                    difficulty = "Easy"
                ),
                DailyQuest(
                    title = "⚔️ Iron Obligation",
                    description = "Successfully complete and log 1 workout session today.",
                    category = "Discipline",
                    xpReward = 40,
                    progressCurrent = 0,
                    progressTarget = 1,
                    difficulty = "Medium"
                ),
                DailyQuest(
                    title = "⚡ Kinetic Charge",
                    description = "Perform 2 minutes of accumulated Plank Hold.",
                    category = "Strength",
                    xpReward = 20,
                    progressCurrent = 0,
                    progressTarget = 2,
                    difficulty = "Easy"
                ),
                DailyQuest(
                    title = "🔮 Mental Fortitude",
                    description = "Mindful stretching session for 5 full minutes.",
                    category = "Consistency",
                    xpReward = 15,
                    progressCurrent = 0,
                    progressTarget = 5,
                    difficulty = "Easy"
                )
            )
            dailyQuestDao.insertQuests(defaultQuests)
            
            if (currentStats != null) {
                userStatsDao.insertUserStats(currentStats.copy(lastQuestResetDateString = today))
            }
        }

        // Seed achievements if empty
        val achievements = achievementDao.getAllAchievementsFlow().firstOrNull() ?: emptyList()
        if (achievements.isEmpty()) {
            val defaultAchievements = listOf(
                Achievement("first_workout", "First Slaying", "Finish your very first workout routine.", 100, false, 0, 1, "", "⚔️"),
                Achievement("level_5", "Asphalt Renegade", "Reach Character Level 5.", 250, false, 1, 5, "", "👑"),
                Achievement("level_10", "Demi-God", "Reach Character Level 10.", 500, false, 1, 10, "", "🔮"),
                Achievement("streak_3", "Unstoppable Force", "Log workouts on 3 consecutive days.", 150, false, 0, 3, "", "⚡"),
                Achievement("streak_7", "Primal Instinct", "Log workouts on 7 consecutive days.", 300, false, 0, 7, "", "🔥"),
                Achievement("calorie_1k", "Metabolic Blaze", "Burn over 1,000 total calories under tracking.", 200, false, 0, 1000, "", "☄️"),
                Achievement("quest_5", "Daily Bounty", "Solve and claim 5 complete Daily Quests.", 150, false, 0, 5, "", "💎")
            )
            achievementDao.insertAchievements(defaultAchievements)
        }
    }

    // --- GAME SERVICES LOGIC ---

    suspend fun incrementDailyQuestProgress(title: String, increment: Int) {
        val quests = dailyQuestDao.getAllQuestsFlow().firstOrNull() ?: emptyList()
        val matching = quests.find { it.title.contains(title, ignoreCase = true) || title.contains(it.title, ignoreCase = true) }
        if (matching != null && !matching.isCompleted) {
            val newProgress = (matching.progressCurrent + increment).coerceAtMost(matching.progressTarget)
            val isNowCompleted = newProgress >= matching.progressTarget
            val updated = matching.copy(progressCurrent = newProgress, isCompleted = isNowCompleted)
            dailyQuestDao.updateQuest(updated)

            if (isNowCompleted) {
                // Claim reward!
                rewardExperience(matching.xpReward)
                incrementStatProgress("Discipline", 0.5f)
                
                // Track achievement progress for Quest Master
                incrementAchievementProgress("quest_5", 1)
            }
        }
    }

    suspend fun incrementStatProgress(statName: String, value: Float) {
        val stats = userStatsDao.getUserStats() ?: return
        val newStats = when (statName.lowercase(Locale.ROOT)) {
            "strength" -> stats.copy(strength = stats.strength + value)
            "endurance" -> stats.copy(endurance = stats.endurance + value)
            "discipline" -> stats.copy(discipline = stats.discipline + value)
            "consistency" -> stats.copy(consistency = stats.consistency + value)
            "agility" -> stats.copy(agility = stats.agility + value)
            else -> stats
        }
        userStatsDao.insertUserStats(newStats)
    }

    suspend fun recordManualWeightLog(weight: Float) {
        // Boost consistency for tracking stats
        incrementStatProgress("Consistency", 0.3f)
    }

    suspend fun completeWorkout(
        routineName: String,
        category: String,
        durationMinutes: Int,
        caloriesBurned: Int,
        expGained: Int,
        exercises: List<Exercise>
    ) {
        val today = getTodayDateString()
        val stats = userStatsDao.getUserStats() ?: UserStats()

        // 1. Log History Entity
        val history = WorkoutHistory(
            routineName = routineName,
            category = category,
            dateString = today,
            timestamp = System.currentTimeMillis(),
            durationMinutes = durationMinutes,
            caloriesBurned = caloriesBurned,
            expEarned = expGained,
            exercisesJson = JsonUtils.exercisesToJson(exercises)
        )
        workoutHistoryDao.insertHistory(history)

        // 2. Compute Streak
        val lastLoggedDate = stats.lastWorkoutDateString
        var newStreak = stats.currentStreak
        if (lastLoggedDate.isBlank()) {
            newStreak = 1
        } else {
            val daysBetween = getDaysBetween(lastLoggedDate, today)
            if (daysBetween == 1) {
                newStreak += 1
            } else if (daysBetween > 1) {
                newStreak = 1 // reset streak
            } // if daysBetween == 0, already logged today, streak remains unchanged!
        }
        val newLongestStreak = maxOf(stats.longestStreak, newStreak)

        // 3. XP Reward & Level Progression
        val oldLevel = stats.level
        val updatedXP = stats.xp + expGained
        
        var tempXP = updatedXP
        var tempLevel = oldLevel
        var tempXPNeeded = tempLevel * 100

        while (tempXP >= tempXPNeeded) {
            tempXP -= tempXPNeeded
            tempLevel += 1
            tempXPNeeded = tempLevel * 100
        }

        // Check if level-up occurred
        if (tempLevel > oldLevel) {
            onLevelUpCallback?.invoke(oldLevel, tempLevel)
        }

        // 4. Update RPG Stats based on layout category
        var strBonus = 0f
        var endBonus = 0f
        var agiBonus = 0f
        var discBonus = 0.4f
        var consBonus = 0.4f

        when (category.lowercase(Locale.ROOT)) {
            "strength" -> strBonus = 0.8f
            "endurance" -> endBonus = 0.8f
            "agility" -> agiBonus = 0.8f
            "discipline" -> discBonus += 0.4f
        }

        val updatedStats = stats.copy(
            level = tempLevel,
            xp = tempXP,
            xpNeeded = tempXPNeeded,
            strength = stats.strength + strBonus,
            endurance = stats.endurance + endBonus,
            agility = stats.agility + agiBonus,
            discipline = stats.discipline + discBonus,
            consistency = stats.consistency + consBonus,
            currentStreak = newStreak,
            longestStreak = newLongestStreak,
            lastWorkoutDateString = today,
            totalWorkouts = stats.totalWorkouts + 1,
            totalCalories = stats.totalCalories + caloriesBurned,
            totalXpEarned = stats.totalXpEarned + expGained,
            title = AvatarUtils.getTitleForLevel(tempLevel)
        )
        userStatsDao.insertUserStats(updatedStats)

        // 5. Update Daily Quests related to workout
        incrementDailyQuestProgress("Iron Obligation", 1)
        if (category == "Strength" && routineName.contains("Core")) {
            incrementDailyQuestProgress("Kinetic Charge", 2)
        }

        // 6. Push Achievement Progresses
        incrementAchievementProgress("first_workout", 1)
        setAchievementProgress("level_5", tempLevel)
        setAchievementProgress("level_10", tempLevel)
        setAchievementProgress("streak_3", newStreak)
        setAchievementProgress("streak_7", newStreak)
        incrementAchievementProgress("calorie_1k", caloriesBurned)
    }

    suspend fun rewardExperience(amount: Int) {
        val stats = userStatsDao.getUserStats() ?: return
        val oldLevel = stats.level
        var tempXP = stats.xp + amount
        var tempLevel = oldLevel
        var tempXPNeeded = tempLevel * 100

        while (tempXP >= tempXPNeeded) {
            tempXP -= tempXPNeeded
            tempLevel += 1
            tempXPNeeded = tempLevel * 100
        }

        if (tempLevel > oldLevel) {
            onLevelUpCallback?.invoke(oldLevel, tempLevel)
        }

        val updated = stats.copy(
            level = tempLevel,
            xp = tempXP,
            xpNeeded = tempXPNeeded,
            totalXpEarned = stats.totalXpEarned + amount,
            title = AvatarUtils.getTitleForLevel(tempLevel)
        )
        userStatsDao.insertUserStats(updated)
    }

    suspend fun updateAvatar(avatarName: String, customName: String) {
        val stats = userStatsDao.getUserStats() ?: return
        userStatsDao.insertUserStats(stats.copy(
            avatarName = avatarName,
            name = customName
        ))
    }

    // --- Core Achievements Tracker ---

    suspend fun incrementAchievementProgress(id: String, increment: Int) {
        val achievement = achievementDao.getAchievementById(id) ?: return
        if (achievement.isUnlocked) return

        val newProgress = (achievement.progressCurrent + increment).coerceAtMost(achievement.progressTarget)
        val isCompletedNow = newProgress >= achievement.progressTarget
        val dateString = if (isCompletedNow) todayReadableString() else ""

        val updated = achievement.copy(
            progressCurrent = newProgress,
            isUnlocked = isCompletedNow,
            unlockedDateString = dateString
        )
        achievementDao.updateAchievement(updated)

        if (isCompletedNow) {
            // Reward experience for unlocking achievement!
            rewardExperience(achievement.xpReward)
        }
    }

    private suspend fun setAchievementProgress(id: String, value: Int) {
        val achievement = achievementDao.getAchievementById(id) ?: return
        if (achievement.isUnlocked) return

        val newProgress = value.coerceAtMost(achievement.progressTarget)
        val isCompletedNow = newProgress >= achievement.progressTarget
        val dateString = if (isCompletedNow) todayReadableString() else ""

        val updated = achievement.copy(
            progressCurrent = newProgress,
            isUnlocked = isCompletedNow,
            unlockedDateString = dateString
        )
        achievementDao.updateAchievement(updated)

        if (isCompletedNow) {
            rewardExperience(achievement.xpReward)
        }
    }

    suspend fun createCustomRoutine(name: String, description: String, category: String, duration: Int, exp: Int, exercises: List<Exercise>) {
        val routine = WorkoutRoutine(
            name = "🛡️ " + name,
            description = description,
            category = category,
            durationMinutesEstimate = duration,
            expReward = exp,
            exercisesJson = JsonUtils.exercisesToJson(exercises),
            isCustom = true
        )
        workoutRoutineDao.insertRoutine(routine)
    }

    suspend fun deleteRoutine(routine: WorkoutRoutine) {
        workoutRoutineDao.deleteRoutine(routine)
    }

    // --- DATE HELPERS ---

    fun getTodayDateString(): String {
        return SimpleDateFormat("yyyy-MM-dd", Locale.ROOT).format(Date())
    }

    private fun todayReadableString(): String {
        return SimpleDateFormat("MMM dd, yyyy", Locale.ROOT).format(Date())
    }

    private fun getDaysBetween(dateStr1: String, dateStr2: String): Int {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ROOT)
            val d1 = sdf.parse(dateStr1) ?: return 0
            val d2 = sdf.parse(dateStr2) ?: return 0
            val diffMs = d2.time - d1.time
            (diffMs / (1000 * 60 * 60 * 24)).toInt()
        } catch (e: Exception) {
            0
        }
    }
}
