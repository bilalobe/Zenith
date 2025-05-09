package com.zenithtasks.domain.usecase

import com.zenithtasks.domain.repository.TaskRepository
import com.zenithtasks.util.SampleTaskGenerator
import kotlinx.coroutines.flow.firstOrNull
import javax.inject.Inject

/**
 * Use case to populate the database with sample tasks if it's empty.
 * This is useful for demonstration purposes or first-time app usage.
 */
class PopulateSampleTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Populate the database with sample tasks if no tasks exist
     * @return true if sample tasks were added, false if the database already had tasks
     */
    suspend fun execute(): Boolean {
        // Check if there are already tasks in the database
        val existingTasks = taskRepository.getAllActiveTasks().firstOrNull()
        
        // If there are no tasks, add sample ones
        if (existingTasks.isNullOrEmpty()) {
            val sampleTasks = SampleTaskGenerator.generateSampleTasks()
            sampleTasks.forEach { task ->
                taskRepository.addTask(task)
            }
            return true
        }
        
        return false
    }
}