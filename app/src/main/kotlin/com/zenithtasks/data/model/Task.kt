package com.zenithtasks.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.zenithtasks.data.local.entity.EnergyLevel
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

    // Whether the task is completed
    val isCompleted: Boolean = false,

    // Due date (if any)
    val dueDate: Date? = null,

    // Creation timestamp
    val createdAt: Date? = Date(),

    // Last update timestamp
    val updatedAt: Date? = Date(),

    // Energy level for the task (default MEDIUM)
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,

    // Priority of the task (default LOW)
    val priority: TaskPriority = TaskPriority.LOW,

    // Reminder date/time (if any)
    val reminder: Date? = null,

    // Whether the reminder has been triggered
    val reminderTriggered: Boolean = false,

    // ID of the associated location (nullable)
    val locationId: Long? = null,

    // Whether location-based reminders are enabled
    val locationReminderEnabled: Boolean = false,

    // Whether the task is archived
    val isArchived: Boolean = false,

    // Firebase document ID for syncing
    val firebaseId: String? = null,

    // Last time this task was synced with the cloud
    val lastSyncedAt: Date? = null,

    // Whether this task has pending changes to be synced
    val pendingSync: Boolean = false,
    
    // Date when the task was completed
    val completedDate: Date? = Date(),
    
    // Whether the task is snoozed
    val isSnoozed: Boolean = false,
    
    // Date when the task should reappear after being snoozed
    val snoozeUntil: Date? = null,
    
    // Count of how many times the task has been snoozed
    val snoozeCount: Int = 0
)
