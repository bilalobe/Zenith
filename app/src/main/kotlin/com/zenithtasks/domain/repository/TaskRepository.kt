package com.zenithtasks.domain.repository

import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.model.Task
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Repository interface for task operations
 */
interface TaskRepository {
    suspend fun addTask(task: Task): Task
    suspend fun updateTask(task: Task)
    suspend fun deleteTask(taskId: Long)
    suspend fun getTaskById(id: Long): Task?
    fun getAllActiveTasks(): Flow<List<Task>>
    fun getActiveTasks(): Flow<List<Task>>
    
    /**
     * Get tasks matching a specific energy level
     */
    fun getTasksByEnergyLevel(energyLevel: EnergyLevel): Flow<List<Task>>
    
    /**
     * Get tasks suitable for low energy states (tasks requiring less energy)
     */
    fun getTasksForLowEnergy(maxEnergyLevel: EnergyLevel): Flow<List<Task>>

    /**
     * Get tasks due today
     */
    suspend fun getTodayTasks(): List<Task>
    
    /**
     * Get incomplete tasks (not completed, not archived)
     */
    suspend fun getIncompleteTasks(): List<Task>
    
    /**
     * Get completed tasks
     */
    suspend fun getCompletedTasks(): List<Task>
    
    /**
     * Get upcoming tasks with limit
     */
    suspend fun getUpcomingTasks(limit: Int): List<Task>
    
    /**
     * Search tasks by title (partial match)
     */
    suspend fun searchTasksByTitle(query: String): List<Task>
    
    /**
     * Mark a task as completed
     */
    suspend fun completeTask(taskId: Long, date: Date)
    
    /**
     * Snooze a task until the specified date
     * @param taskId ID of the task to snooze
     * @param snoozeUntil Date when the task should reappear
     */
    suspend fun snoozeTask(taskId: Long, snoozeUntil: Long)
    
    /**
     * Un-snooze a task, making it immediately visible again
     * @param taskId ID of the task to un-snooze
     */
    suspend fun unsnoozeTask(taskId: Long)
    
    /**
     * Get all snoozed tasks
     */
    suspend fun getSnoozedTasks(): List<Task>
    
    /**
     * Check and un-snooze any tasks whose snooze period has expired
     */
    suspend fun checkAndUnsnoozeExpiredTasks()
    
    /**
     * Get active tasks for widget display
     */
    fun getActiveTasksForWidget(): Flow<List<Task>>
}