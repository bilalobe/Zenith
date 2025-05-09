package com.zenithtasks.domain.usecase

import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import javax.inject.Inject

/**
 * Use case to retrieve a specific task by its ID
 */
class GetTaskByIdUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long): Task? {
        return taskRepository.getTaskById(taskId)
    }
}