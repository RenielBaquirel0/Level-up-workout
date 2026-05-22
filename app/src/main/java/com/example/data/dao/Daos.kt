package com.example.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.UserStats
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutHistory
import com.example.data.model.DailyQuest
import com.example.data.model.Achievement
import kotlinx.coroutines.flow.Flow

@Dao
interface UserStatsDao {
    @Query("SELECT * FROM user_stats WHERE id = 1")
    fun getUserStatsFlow(): Flow<UserStats?>

    @Query("SELECT * FROM user_stats WHERE id = 1")
    suspend fun getUserStats(): UserStats?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserStats(stats: UserStats)
}

@Dao
interface WorkoutRoutineDao {
    @Query("SELECT * FROM workout_routines ORDER BY name ASC")
    fun getAllRoutinesFlow(): Flow<List<WorkoutRoutine>>

    @Query("SELECT * FROM workout_routines WHERE id = :id")
    suspend fun getRoutineById(id: Int): WorkoutRoutine?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRoutine(routine: WorkoutRoutine): Long

    @Delete
    suspend fun deleteRoutine(routine: WorkoutRoutine)

    @Query("SELECT COUNT(*) FROM workout_routines")
    suspend fun getCount(): Int
}

@Dao
interface WorkoutHistoryDao {
    @Query("SELECT * FROM workout_history ORDER BY timestamp DESC")
    fun getAllHistoryFlow(): Flow<List<WorkoutHistory>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(history: WorkoutHistory): Long

    @Query("DELETE FROM workout_history")
    suspend fun clearHistory()
}

@Dao
interface DailyQuestDao {
    @Query("SELECT * FROM daily_quests")
    fun getAllQuestsFlow(): Flow<List<DailyQuest>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertQuests(quests: List<DailyQuest>)

    @Update
    suspend fun updateQuest(quest: DailyQuest)

    @Query("DELETE FROM daily_quests")
    suspend fun deleteQuests()
}

@Dao
interface AchievementDao {
    @Query("SELECT * FROM achievements")
    fun getAllAchievementsFlow(): Flow<List<Achievement>>

    @Query("SELECT * FROM achievements WHERE id = :id")
    suspend fun getAchievementById(id: String): Achievement?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAchievements(achievements: List<Achievement>)

    @Update
    suspend fun updateAchievement(achievement: Achievement)
}
