package com.zenithtasks.domain.usecase

import com.zenithtasks.domain.repository.TaskRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case to mark a task as complete
 */
class CompleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, isCompleted: Boolean, completedDate: Date) {
        // Get the task first, then update its completion status
        val task = taskRepository.getTaskById(taskId)
        if (task != null) {
            taskRepository.updateTask(
                task.copy(
                    isCompleted = isCompleted,
                    completedDate = completedDate
                )
            )
        }
    }
}