package com.example.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

// ---------------- USER STATS ENTITY ----------------
@Entity(tableName = "user_stats")
data class UserStats(
    @PrimaryKey val id: Int = 1, // Single profile local user
    val name: String = "Slayer",
    val title: String = "Novice Recruit",
    val avatarName: String = "avatar_ninja",
    val level: Int = 1,
    val xp: Int = 0,
    val xpNeeded: Int = 100,
    
    // RPG-Style Core Attribute Stats
    val strength: Float = 10f,
    val endurance: Float = 10f,
    val discipline: Float = 10f,
    val consistency: Float = 10f,
    val agility: Float = 10f,
    
    // Streak Tracking
    val currentStreak: Int = 0,
    val longestStreak: Int = 0,
    val lastWorkoutDateString: String = "", // YYYY-MM-DD
    val lastQuestResetDateString: String = "", // YYYY-MM-DD
    
    // Career Metrics
    val totalWorkouts: Int = 0,
    val totalCalories: Int = 0,
    val totalXpEarned: Int = 0
)

// Helper mapping for avatars
object AvatarUtils {
    val AVATARS = listOf(
        "avatar_ninja" to "🥷 Shadow Assassin",
        "avatar_knight" to "🛡️ Steel Sentinel",
        "avatar_mage" to "🔮 Mystic Sage",
        "avatar_barbarian" to "🪓 Berserker",
        "avatar_ranger" to "🏹 Elite Ranger",
        "avatar_cyber" to "👾 Neon Runner"
    )
    val TITLES = listOf(
        1 to "Novice Recruit",
        3 to "Asphalt Warrior",
        5 to "Iron Discipline",
        10 to "Gym Gladiator",
        15 to "Hyper-focused Titan",
        20 to "Demi-God of Iron",
        30 to "Supreme Overlord"
    )
    
    fun getTitleForLevel(level: Int): String {
        return TITLES.lastOrNull { level >= it.first }?.second ?: "Novice Recruit"
    }
}

// ---------------- WORKOUT ROUTINE ENTITY ----------------
@Entity(tableName = "workout_routines")
data class WorkoutRoutine(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val name: String,
    val description: String,
    val category: String, // e.g., "Strength", "Endurance", "Agility", "Discipline"
    val durationMinutesEstimate: Int,
    val expReward: Int,
    val exercisesJson: String, // JSON string representing List<Exercise>
    val isCustom: Boolean = false
)

// Non-entity structures serialized as JSON in the DB
data class Exercise(
    val name: String,
    val targetReps: Int,
    val targetSets: Int,
    val restSeconds: Int = 60,
    val exercisesDone: Int = 0,
    val sets: List<WorkoutSet> = emptyList()
)

data class WorkoutSet(
    val setNumber: Int,
    val repsCount: Int,
    val weightKg: Float,
    val isCompleted: Boolean = false
)

// ---------------- WORKOUT HISTORY ENTITY ----------------
@Entity(tableName = "workout_history")
data class WorkoutHistory(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val routineName: String,
    val category: String,
    val dateString: String, // YYYY-MM-DD
    val timestamp: Long,
    val durationMinutes: Int,
    val caloriesBurned: Int,
    val expEarned: Int,
    val exercisesJson: String // Logged completed exercise sets
)

// ---------------- DAILY QUEST ENTITY ----------------
@Entity(tableName = "daily_quests")
data class DailyQuest(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val category: String, // "Strength", "Endurance", "Discipline", "Consistency", "Agility"
    val xpReward: Int,
    val progressCurrent: Int,
    val progressTarget: Int,
    val isCompleted: Boolean = false,
    val difficulty: String = "Easy" // "Easy", "Medium", "Hard"
)

// ---------------- ACHIEVEMENT ENTITY ----------------
@Entity(tableName = "achievements")
data class Achievement(
    @PrimaryKey val id: String, // e.g., "first_steps", "beast_mode"
    val title: String,
    val description: String,
    val xpReward: Int,
    val isUnlocked: Boolean = false,
    val progressCurrent: Int = 0,
    val progressTarget: Int = 1,
    val unlockedDateString: String = "",
    val iconEmoji: String // emoji representing the achievement badge
)
