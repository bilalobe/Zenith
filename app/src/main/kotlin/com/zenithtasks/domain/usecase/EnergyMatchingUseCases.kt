package com.zenithtasks.domain.usecase

import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.model.Task
import com.zenithtasks.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case to get tasks based on user's current energy level.
 * When user has low energy, we get tasks requiring less energy.
 */
class GetTasksByUserEnergyLevelUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(userEnergyLevel: EnergyLevel): Flow<List<Task>> {
        return when (userEnergyLevel) {
            EnergyLevel.LOW -> taskRepository.getTasksForLowEnergy(EnergyLevel.LOW)
            EnergyLevel.MEDIUM -> taskRepository.getTasksByEnergyLevel(EnergyLevel.MEDIUM)
            EnergyLevel.HIGH -> taskRepository.getAllActiveTasks()
        }
    }
}

/**
 * Use case to suggest tasks based on time of day.
 * Later in the day, suggest easier tasks as people tend to have less energy.
 */
class SuggestTasksByTimeOfDayUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    operator fun invoke(hourOfDay: Int): Flow<List<Task>> {
        // Early morning (5-9): Medium energy
        // Morning to early afternoon (9-15): High energy
        // Afternoon (15-19): Medium energy
        // Evening/Night (19-5): Low energy
        return when (hourOfDay) {
            in 5..8 -> taskRepository.getTasksByEnergyLevel(EnergyLevel.MEDIUM)
            in 9..14 -> taskRepository.getAllActiveTasks() // All tasks, including high energy ones
            in 15..18 -> taskRepository.getTasksByEnergyLevel(EnergyLevel.MEDIUM)
            else -> taskRepository.getTasksForLowEnergy(EnergyLevel.LOW) // Evening/night: low energy tasks
        }
    }
}