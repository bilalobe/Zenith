package com.zenithtasks.domain.usecase

import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * Use case for deleting a task from the database.
 * This represents a domain-level operation to permanently remove a task.
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Delete a task permanently from the database
     * 
     * @param task The task to delete
     */
    suspend operator fun invoke(task: Long) = withContext(Dispatchers.IO) {
        taskRepository.deleteTask(task)
    }
}