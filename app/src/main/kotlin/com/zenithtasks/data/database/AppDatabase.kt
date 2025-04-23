package com.zenithtasks.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.zenithtasks.data.dao.LocationDao
import com.zenithtasks.data.dao.TaskDao
import com.zenithtasks.data.model.Location
import com.zenithtasks.data.model.Task

/**
 * Main database for the application.
 * Defines the database configuration and serves as the app's main access point to persisted data.
 */
@Database(
    entities = [Task::class, Location::class],
    version = 1,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {

    /**
     * Gets the DAO for Task operations.
     */
    abstract fun taskDao(): TaskDao

    /**
     * Gets the DAO for Location operations.
     */
    abstract fun locationDao(): LocationDao

    companion object {
        // Singleton prevents multiple instances of the database opening at the same time
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
            // If the instance is not null, return it, otherwise create the database
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "zenith_database"
                )
                    .fallbackToDestructiveMigration() // Wipes and rebuilds instead of migrating if no Migration object
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
