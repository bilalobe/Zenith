package com.zenithtasks.domain.usecase

import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to retrieve all active tasks
 */
class GetTasksUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> {
        return taskRepository.getActiveTasks()
    }
}