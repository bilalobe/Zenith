package com.zenithtasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.local.entity.TaskEntity
import com.zenithtasks.data.model.TaskWithLocation
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for Task entities.
 * Provides methods to interact with the tasks table.
 */
@Dao
interface TaskDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: TaskEntity): Long

    @Update
    suspend fun updateTask(task: TaskEntity)

    @Delete
    suspend fun deleteTask(task: TaskEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskById(taskId: Long): TaskEntity?

    @Query("SELECT * FROM tasks WHERE isArchived = 0 ORDER BY dueDate ASC, priority DESC")
    fun getAllActiveTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE isCompleted = 0 AND isArchived = 0 ORDER BY dueDate ASC, priority DESC")
    fun getActiveTasks(): Flow<List<TaskEntity>>

    /**
     * Get tasks by energy level, for energy-based matching.
     */
    @Query("SELECT * FROM tasks WHERE energyLevel = :energyLevel AND isCompleted = 0 AND isArchived = 0 ORDER BY dueDate ASC, priority DESC")
    fun getTasksByEnergyLevel(energyLevel: EnergyLevel): Flow<List<TaskEntity>>

    /**
     * Get tasks with energy level less than or equal to the given level.
     * Used when the user has low energy and needs easier tasks.
     */
    @Query("SELECT * FROM tasks WHERE energyLevel <= :maxEnergyLevel AND isCompleted = 0 AND isArchived = 0 ORDER BY energyLevel ASC, dueDate ASC")
    fun getTasksForLowEnergy(maxEnergyLevel: EnergyLevel): Flow<List<TaskEntity>>

    /**
     * Get tasks for a specific location
     */
    @Query("SELECT * FROM tasks WHERE locationId = :locationId AND isCompleted = 0 AND isArchived = 0")
    fun getTasksByLocationId(locationId: Long): Flow<List<TaskEntity>>

    /**
     * Update the reminder triggered status for a task
     */
    @Query("UPDATE tasks SET reminderTriggered = :triggered, updatedAt = CURRENT_TIMESTAMP WHERE id = :taskId")
    suspend fun updateReminderTriggered(taskId: Long, triggered: Boolean)

    /**
     * Insert multiple tasks into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<TaskEntity>): List<Long>

    /**
     * Get tasks that have been modified since the specified time.
     */
    @Query("SELECT * FROM tasks WHERE updatedAt > :since OR createdAt > :since")
    suspend fun getTasksModifiedSince(since: Date): List<TaskEntity>

    /**
     * Get tasks with location reminders enabled, along with their locations.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE locationReminderEnabled = 1 AND isCompleted = 0")
    suspend fun getTasksWithLocationRemindersAndLocation(): List<TaskWithLocation>

    /**
     * Get all tasks for a specific location as a Flow.
     */
    @Query("SELECT * FROM tasks WHERE locationId = :locationId ORDER BY createdAt DESC")
    fun getTasksByLocationFlow(locationId: Long): Flow<List<TaskEntity>>

    /**
     * Mark a task's reminder as triggered.
     */
    @Query("UPDATE tasks SET reminderTriggered = 1, updatedAt = CURRENT_TIMESTAMP WHERE id = :id")
    suspend fun markReminderAsTriggered(id: Long)
}