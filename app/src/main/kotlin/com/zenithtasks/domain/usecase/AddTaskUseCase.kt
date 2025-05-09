package com.zenithtasks.domain.usecase

import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

/**
 * Use case for adding new tasks to the database.
 * This represents a domain-level operation for task creation.
 */
class AddTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Creates a new task in the database
     * 
     * @param task The task to add
     * @return The created task with its generated ID
     */
    suspend operator fun invoke(task: Task): Task = withContext(Dispatchers.IO) {
        // Ensure created and updated timestamps are set
        val taskToAdd = task.copy(
            createdAt = task.createdAt ?: Date(),
            updatedAt = task.updatedAt ?: Date(),
            pendingSync = true
        )
        
        // Add the task to the repository and return the result
        taskRepository.addTask(taskToAdd)
    }
    
    /**
     * Convenience method to create a task with minimal information
     * 
     * @param title The title of the task
     * @param description Optional description
     * @param energyLevel Energy level required for the task
     * @param dueDate Optional due date
     * @return The created task with generated ID
     */
    suspend fun createTask(
        title: String,
        description: String? = null,
        energyLevel: com.zenithtasks.data.local.entity.EnergyLevel = com.zenithtasks.data.local.entity.EnergyLevel.MEDIUM,
        dueDate: Date? = null,
        locationId: Long? = null
    ): Task = withContext(Dispatchers.IO) {
        val now = Date()
        val task = Task(
            title = title,
            description = description,
            dueDate = dueDate,
            createdAt = now,
            updatedAt = now,
            energyLevel = energyLevel,
            locationId = locationId,
            pendingSync = true,
            completedDate = Date(),
        )
        
        taskRepository.addTask(task)
    }
}