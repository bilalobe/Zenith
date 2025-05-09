package com.zenithtasks.domain.usecase

import com.zenithtasks.data.model.SnoozeOptions
import com.zenithtasks.domain.repository.TaskRepository
import java.util.Date
import javax.inject.Inject

/**
 * Use case for snoozing tasks
 */
class SnoozeTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Snooze a task using a predefined snooze option
     * @param taskId ID of the task to snooze
     * @param option The snooze option to use
     */
    suspend operator fun invoke(taskId: Long, option: SnoozeOptions) {
        val snoozeDate = option.calculateSnoozeUntil()
        taskRepository.snoozeTask(taskId, snoozeDate.time)
    }
    
    /**
     * Snooze a task until a specific date
     * @param taskId ID of the task to snooze
     * @param snoozeUntil Date when the task should reappear
     */
    suspend operator fun invoke(taskId: Long, snoozeUntil: Date) {
        taskRepository.snoozeTask(taskId, snoozeUntil.time)
    }
    
    /**
     * Un-snooze a task, making it visible again immediately
     * @param taskId ID of the task to un-snooze
     */
    suspend fun unsnooze(taskId: Long) {
        taskRepository.unsnoozeTask(taskId)
    }
    
    /**
     * Check and un-snooze any tasks whose snooze period has expired
     */
    suspend fun checkAndUnsnoozeExpiredTasks() {
        taskRepository.checkAndUnsnoozeExpiredTasks()
    }
}