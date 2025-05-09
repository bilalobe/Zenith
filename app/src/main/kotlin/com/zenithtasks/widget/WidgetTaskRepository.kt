package com.zenithtasks.widget

import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository to handle widget-specific task operations.
 */
@Singleton
class WidgetTaskRepository @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Completes a task with the given ID.
     */
    suspend fun completeTask(taskId: Long) {
        taskRepository.completeTask(taskId, Date())
    }

    /**
     * Updates a widget-specific filter (e.g., show high priority tasks only)
     */
    suspend fun updateWidgetFilter(appWidgetId: Int, filterType: Int) {
        // TODO: Implement storing and retrieving widget preferences
    }

    /**
     * Get tasks filtered specifically for widget display
     */
    fun getActiveTasksForWidget(): Flow<List<Task>> {
        return taskRepository.getActiveTasksForWidget()
    }
}