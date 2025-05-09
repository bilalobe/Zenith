package com.zenithtasks.data.repository

import com.zenithtasks.data.local.dao.TaskDao
import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.local.entity.TaskEntity
import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import java.util.Date
import javax.inject.Inject

/**
 * Implementation of TaskRepository using Room database
 */
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao
) : TaskRepository {

    override suspend fun addTask(task: Task): Task {
        val id = taskDao.insertTask(task.toEntity())
        return task.copy(id = id)
    }

    override suspend fun updateTask(task: Task) {
        taskDao.updateTask(task.toEntity())
    }

    override suspend fun deleteTask(taskId: Long) {
        val taskEntity = taskDao.getTaskById(taskId)
        if (taskEntity != null) {
            taskDao.deleteTask(taskEntity)
        }
    }

    override suspend fun getTaskById(id: Long): Task? {
        return taskDao.getTaskById(id)?.toDomain()
    }

    override fun getAllActiveTasks(): Flow<List<Task>> {
        return taskDao.getAllActiveTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getActiveTasks(): Flow<List<Task>> {
        return taskDao.getActiveTasks().map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTasksByEnergyLevel(energyLevel: EnergyLevel): Flow<List<Task>> {
        return taskDao.getTasksByEnergyLevel(energyLevel).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    override fun getTasksForLowEnergy(maxEnergyLevel: EnergyLevel): Flow<List<Task>> {
        return taskDao.getTasksForLowEnergy(maxEnergyLevel).map { entities ->
            entities.map { it.toDomain() }
        }
    }
    
    /**
     * Get tasks due today
     */
    override suspend fun getTodayTasks(): List<Task> {
        // Get today's tasks - we'll get all active tasks and filter for tasks due today
        val calendar = java.util.Calendar.getInstance()
        // Reset time to start of day
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
        calendar.set(java.util.Calendar.MINUTE, 0)
        calendar.set(java.util.Calendar.SECOND, 0)
        calendar.set(java.util.Calendar.MILLISECOND, 0)
        val startOfDay = calendar.time
        
        // Set to end of day
        calendar.set(java.util.Calendar.HOUR_OF_DAY, 23)
        calendar.set(java.util.Calendar.MINUTE, 59)
        calendar.set(java.util.Calendar.SECOND, 59)
        calendar.set(java.util.Calendar.MILLISECOND, 999)
        val endOfDay = calendar.time
        
        val tasks = taskDao.getActiveTasks().first()
        return tasks.filter { task ->
            task.dueDate?.let { dueDate ->
                dueDate in startOfDay..endOfDay
            } == true
        }.map { it.toDomain() }
    }
    
    /**
     * Get incomplete tasks (not completed, not archived)
     */
    override suspend fun getIncompleteTasks(): List<Task> {
        return taskDao.getActiveTasks().first().map { it.toDomain() }
    }
    
    /**
     * Get completed tasks
     */
    override suspend fun getCompletedTasks(): List<Task> {
        return taskDao.getAllActiveTasks().first().filter { it.isCompleted }.map { it.toDomain() }
    }
    
    /**
     * Get upcoming tasks with limit
     */
    override suspend fun getUpcomingTasks(limit: Int): List<Task> {
        val now = Date()
        return taskDao.getActiveTasks().first()
            .filter { task -> 
                !task.isCompleted && (task.dueDate == null || task.dueDate > now)
            }
            .sortedBy { it.dueDate }
            .take(limit)
            .map { it.toDomain() }
    }
    
    /**
     * Search tasks by title (partial match)
     */
    override suspend fun searchTasksByTitle(query: String): List<Task> {
        val searchPattern = "%${query.trim().lowercase()}%"
        return taskDao.searchByTitle(searchPattern).map { it.toDomain() }
    }
    
    /**
     * Mark a task as completed
     */
    override suspend fun completeTask(taskId: Long, date: Date) {
        val task = taskDao.getTaskById(taskId) ?: return
        val updatedTask = task.copy(
            isCompleted = true,
            updatedAt = Date()
        )
        taskDao.updateTask(updatedTask)
    }

    /**
     * Snooze a task until the specified date
     */
    override suspend fun snoozeTask(taskId: Long, snoozeUntil: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        val snoozeDate = Date(snoozeUntil)
        val updatedTask = task.copy(
            isSnoozed = true,
            snoozeUntil = snoozeDate,
            snoozeCount = task.snoozeCount.plus(1),
            updatedAt = Date()
        )
        taskDao.updateTask(updatedTask)
    }
    
    /**
     * Un-snooze a task, making it immediately visible again
     */
    override suspend fun unsnoozeTask(taskId: Long) {
        val task = taskDao.getTaskById(taskId) ?: return
        val updatedTask = task.copy(
            isSnoozed = false,
            snoozeUntil = null,
            updatedAt = Date()
        )
        taskDao.updateTask(updatedTask)
    }
    
    /**
     * Get all snoozed tasks
     */
    override suspend fun getSnoozedTasks(): List<Task> {
        return taskDao.getAllActiveTasks().first()
            .filter { it.isSnoozed && !it.isCompleted && !it.isArchived }
            .map { it.toDomain() }
    }
    
    /**
     * Check and un-snooze any tasks whose snooze period has expired
     */
    override suspend fun checkAndUnsnoozeExpiredTasks() {
        val now = Date()
        val tasks = taskDao.getAllActiveTasks().first()
            .filter { it.isSnoozed && it.snoozeUntil != null && it.snoozeUntil <= now }
        
        tasks.forEach { task ->
            val updatedTask = task.copy(
                isSnoozed = false,
                updatedAt = now
            )
            taskDao.updateTask(updatedTask)
        }
    }

    /**
     * Get active tasks for the widget display
     */
    override fun getActiveTasksForWidget(): Flow<List<Task>> {
        // Return a flow of non-completed, non-archived tasks for widget display
        return taskDao.getActiveTasks()
            .map { tasks ->
                tasks.filter { !it.isSnoozed } // Only show tasks that aren't snoozed
                    .sortedWith(
                        compareBy<TaskEntity> { it.dueDate == null }
                            .thenBy { it.dueDate }
                            .thenByDescending { it.priority }
                    )
                    .map { it.toDomain() }
            }
    }

    // Extension functions for mapping between domain and data models
    private fun Task.toEntity(): TaskEntity {
        return TaskEntity(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            dueDate = dueDate,
            createdAt = createdAt,
            energyLevel = energyLevel,
            priority = priority,
            isArchived = isArchived,
            reminderTriggered = reminderTriggered,
            locationId = locationId,
            locationReminderEnabled = locationReminderEnabled,
            updatedAt = updatedAt,
            isSnoozed = isSnoozed,
            snoozeUntil = snoozeUntil,
            snoozeCount = snoozeCount
        )
    }
    
    private fun TaskEntity.toDomain(): Task {
        return Task(
            id = id,
            title = title,
            description = description,
            isCompleted = isCompleted,
            dueDate = dueDate,
            createdAt = createdAt,
            updatedAt = updatedAt,
            energyLevel = energyLevel,
            priority = priority,
            reminderTriggered = reminderTriggered,
            locationId = locationId,
            locationReminderEnabled = locationReminderEnabled,
            isArchived = isArchived,
            reminder = null,
            firebaseId = null,
            lastSyncedAt = null,
            pendingSync = false,
            completedDate = Date(), // Default to current date
            isSnoozed = isSnoozed,
            snoozeUntil = snoozeUntil,
            snoozeCount = snoozeCount
        )
    }
}