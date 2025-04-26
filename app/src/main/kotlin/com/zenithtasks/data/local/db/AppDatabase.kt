package com.zenithtasks.data.local.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zenithtasks.data.local.dao.FocusSessionDao
import com.zenithtasks.data.local.dao.LocationDao
import com.zenithtasks.data.local.dao.TaskDao
import com.zenithtasks.data.local.entity.TaskEntity
import com.zenithtasks.data.model.FocusSession
import com.zenithtasks.data.model.Location

/**
 * Main database for the application.
 */
@Database(
    entities = [TaskEntity::class, Location::class, FocusSession::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(DateConverters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun locationDao(): LocationDao
    abstract fun focusSessionDao(): FocusSessionDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        /**
         * Gets the singleton instance of the database.
         * If the instance doesn't exist, it creates a new one.
         *
         * @param context The application context
         * @return The singleton instance of the database
         */
        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                )
                    .fallbackToDestructiveMigration(false)
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}