package com.zenithtasks.domain.usecase

import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case to update an existing task
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        taskRepository.updateTask(
            task.copy(
                updatedAt = Date(),
                pendingSync = true
            )
        )
    }
}