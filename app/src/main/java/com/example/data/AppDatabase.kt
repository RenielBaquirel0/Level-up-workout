package com.example.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.data.dao.UserStatsDao
import com.example.data.dao.WorkoutRoutineDao
import com.example.data.dao.WorkoutHistoryDao
import com.example.data.dao.DailyQuestDao
import com.example.data.dao.AchievementDao
import com.example.data.model.UserStats
import com.example.data.model.WorkoutRoutine
import com.example.data.model.WorkoutHistory
import com.example.data.model.DailyQuest
import com.example.data.model.Achievement

@Database(
    entities = [
        UserStats::class,
        WorkoutRoutine::class,
        WorkoutHistory::class,
        DailyQuest::class,
        Achievement::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userStatsDao(): UserStatsDao
    abstract fun workoutRoutineDao(): WorkoutRoutineDao
    abstract fun workoutHistoryDao(): WorkoutHistoryDao
    abstract fun dailyQuestDao(): DailyQuestDao
    abstract fun achievementDao(): AchievementDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "levelup_workout_db"
                )
                .fallbackToDestructiveMigration()
                .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
