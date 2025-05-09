package com.zenithtasks.domain.usecase

import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject

/**
 * Use case for archiving tasks.
 * This represents a domain-level operation to mark a task as archived rather than deleting it,
 * allowing users to reference it later if needed.
 */
class ArchiveTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    /**
     * Archive a task by marking it as archived and updating it in the repository
     * 
     * @param task The task to archive
     * @return The updated archived task
     */
    suspend operator fun invoke(task: Task): Task = withContext(Dispatchers.IO) {
        // Create a copy of the task with isArchived=true and updated timestamp
        val archivedTask = task.copy(
            isArchived = true,
            updatedAt = Date(),
            pendingSync = true // Mark for syncing with remote storage if applicable
        )
        
        // Update the task in the repository
        taskRepository.updateTask(archivedTask)
        
        return@withContext archivedTask
    }
    
    /**
     * Unarchive a task by marking it as not archived
     * 
     * @param task The task to unarchive
     * @return The updated unarchived task
     */
    suspend fun unarchive(task: Task): Task = withContext(Dispatchers.IO) {
        // Create a copy of the task with isArchived=false and updated timestamp
        val unarchivedTask = task.copy(
            isArchived = false,
            updatedAt = Date(),
            pendingSync = true // Mark for syncing with remote storage if applicable
        )
        
        // Update the task in the repository
        taskRepository.updateTask(unarchivedTask)
        
        return@withContext unarchivedTask
    }
}