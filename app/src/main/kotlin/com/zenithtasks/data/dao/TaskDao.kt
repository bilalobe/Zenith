package com.zenithtasks.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskWithLocation
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Task entity.
 * Provides methods to interact with the tasks table in the database.
 */
@Dao
interface TaskDao {

    /**
     * Get all tasks as a Flow.
     * @return Flow of all tasks in the database.
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksFlow(): Flow<List<Task>>

    /**
     * Get all tasks as a list.
     * @return List of all tasks in the database.
     */
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    suspend fun getAllTasks(): List<Task>

    /**
     * Get a task by its ID.
     * @param id The ID of the task to retrieve.
     * @return The task with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM tasks WHERE id = :id")
    suspend fun getTaskById(id: Long): Task?

    /**
     * Get all tasks for a specific location.
     * @param locationId The ID of the location.
     * @return Flow of all tasks associated with the specified location.
     */
    @Query("SELECT * FROM tasks WHERE locationId = :locationId ORDER BY createdAt DESC")
    fun getTasksByLocationFlow(locationId: Long): Flow<List<Task>>

    /**
     * Get all tasks with location reminders enabled.
     * @return List of all tasks with location reminders enabled.
     */
    @Query("SELECT * FROM tasks WHERE locationReminderEnabled = 1 AND isCompleted = 0")
    suspend fun getTasksWithLocationReminders(): List<Task>

    /**
     * Insert a new task into the database.
     * @param task The task to insert.
     * @return The ID of the newly inserted task.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTask(task: Task): Long

    /**
     * Insert multiple tasks into the database.
     * @param tasks The list of tasks to insert.
     * @return The list of IDs of the newly inserted tasks.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertTasks(tasks: List<Task>): List<Long>

    /**
     * Update an existing task in the database.
     * @param task The task to update.
     */
    @Update
    suspend fun updateTask(task: Task)

    /**
     * Delete a task from the database.
     * @param task The task to delete.
     */
    @Delete
    suspend fun deleteTask(task: Task)

    /**
     * Delete a task by its ID.
     * @param id The ID of the task to delete.
     */
    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteTaskById(id: Long)

    /**
     * Mark a task as completed.
     * @param id The ID of the task to mark as completed.
     */
    @Query("UPDATE tasks SET isCompleted = 1, updatedAt = datetime('now') WHERE id = :id")
    suspend fun markTaskAsCompleted(id: Long)

    /**
     * Mark a task's reminder as triggered.
     * @param id The ID of the task to mark as triggered.
     */
    @Query("UPDATE tasks SET reminderTriggered = 1, updatedAt = datetime('now') WHERE id = :id")
    suspend fun markReminderAsTriggered(id: Long)

    /**
     * Reset all reminder triggered flags (e.g., when user leaves a location).
     * @param locationId The ID of the location for which to reset reminders.
     */
    @Query("UPDATE tasks SET reminderTriggered = 0, updatedAt = datetime('now') WHERE locationId = :locationId")
    suspend fun resetRemindersForLocation(locationId: Long)

    /**
     * Get a task with its associated location by task ID.
     * @param taskId The ID of the task to retrieve.
     * @return The task with its location, or null if not found.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    suspend fun getTaskWithLocation(taskId: Long): TaskWithLocation?

    /**
     * Get all tasks with their associated locations.
     * @return Flow of all tasks with their locations.
     */
    @Transaction
    @Query("SELECT * FROM tasks ORDER BY createdAt DESC")
    fun getAllTasksWithLocationFlow(): Flow<List<TaskWithLocation>>

    /**
     * Get all tasks with location reminders enabled, along with their locations.
     * @return List of all tasks with location reminders enabled, along with their locations.
     */
    @Transaction
    @Query("SELECT * FROM tasks WHERE locationReminderEnabled = 1 AND isCompleted = 0")
    suspend fun getTasksWithLocationRemindersAndLocation(): List<TaskWithLocation>
}
