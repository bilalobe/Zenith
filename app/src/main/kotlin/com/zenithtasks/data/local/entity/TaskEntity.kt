package com.zenithtasks.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.zenithtasks.data.model.TaskPriority
import java.util.Date

/**
 * Entity representing a task in the database.
 * Includes an energy level field for energy-based task matching.
 */
@Entity(tableName = "tasks")
data class TaskEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val description: String? = null,
    val isCompleted: Boolean = false,
    val dueDate: Date? = null,
    val createdAt: Date? = Date(),
    val energyLevel: EnergyLevel = EnergyLevel.MEDIUM,
    val priority: TaskPriority = TaskPriority.LOW,
    val isArchived: Boolean = false,
    val locationId: Long? = null,
    val locationReminderEnabled: Boolean = false,
    val reminderTriggered: Boolean = false,
    val updatedAt: Date? = Date(),
    // Snooze related fields
    val isSnoozed: Boolean = false,
    val snoozeUntil: Date? = null,
    val snoozeCount: Int = 0
)

/**
 * Enum representing different energy levels for tasks.
 * Used for matching tasks to user energy level.
 */
enum class EnergyLevel {
    LOW, MEDIUM, HIGH;
}