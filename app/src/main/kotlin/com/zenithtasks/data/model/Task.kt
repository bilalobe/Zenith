package com.zenithtasks.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a task that can be associated with a location.
 * When the user arrives at the associated location, they will receive a reminder for this task.
 */
@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = Location::class,
            parentColumns = ["id"],
            childColumns = ["locationId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [Index("locationId")]
)
data class Task(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Title of the task
    val title: String,
    
    // Optional description
    val description: String? = null,
    
    // Due date (if any)
    val dueDate: Date? = null,
    
    // Whether the task is completed
    val isCompleted: Boolean = false,
    
    // ID of the associated location (nullable - not all tasks need a location)
    val locationId: Long? = null,
    
    // Whether location-based reminders are enabled for this task
    val locationReminderEnabled: Boolean = false,
    
    // Whether the reminder has been triggered for this location
    val reminderTriggered: Boolean = false,
    
    // Creation timestamp
    val createdAt: Date = Date(),
    
    // Last update timestamp
    val updatedAt: Date = Date()
)