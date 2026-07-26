package com.ash.healthtracker.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MigraineEpisode::class, WaterLog::class, MedicationLog::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun migraineDao(): MigraineDao
    abstract fun waterDao(): WaterDao
    abstract fun medicationDao(): MedicationDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "health_tracker.db"
                ).build().also { INSTANCE = it }
            }
        }
    }
}
