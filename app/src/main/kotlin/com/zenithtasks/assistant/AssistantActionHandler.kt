package com.zenithtasks.assistant

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskPriority
import com.zenithtasks.domain.repository.TaskRepository
import com.zenithtasks.focus.FocusControlService
import com.zenithtasks.focus.FocusRepository
import com.zenithtasks.focus.FocusState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Handles deep linking from Google Assistant/Gemini intents defined in actions.xml.
 * Routes Assistant actions to the appropriate service or repository.
 */
@AndroidEntryPoint
class AssistantActionHandler : BroadcastReceiver() {

    @Inject
    lateinit var taskRepository: TaskRepository

    @Inject
    lateinit var focusRepository: FocusRepository

    companion object {
        // These action strings should match what's defined in your actions.xml
        // and ideally align with Built-in Intents (BIIs)
        // Example BIIs:
        const val ACTION_CREATE_TASK = "actions.intent.CREATE_TASK" // BII for creating tasks
        const val ACTION_START_FOCUS = "com.zenithtasks.intent.action.START_FOCUS" // Custom intent for focus
        const val ACTION_GET_TASKS = "actions.intent.GET_TASK" // BII for getting tasks
        const val ACTION_COMPLETE_TASK = "com.zenithtasks.intent.action.COMPLETE_TASK" // Custom or use UPDATE_TASK BII

        // Parameter names should align with BIIs or your custom intent definitions
        // For CREATE_TASK BII:
        const val EXTRA_TASK_NAME = "name" // Common parameter for BIIs
        const val EXTRA_TASK_DESCRIPTION = "description" // Common parameter for BIIs
        // For due date, BIIs might use more structured date/time parameters
        const val EXTRA_TASK_DUE_DATE_TEXT = "dueDateText" // Example for free-text due date

        // For custom START_FOCUS intent:
        const val EXTRA_FOCUS_DURATION_MINUTES = "durationMinutes"

        // For GET_TASK BII:
        const val EXTRA_TASK_STATUS_FILTER = "taskStatus" // e.g., "ACTIVE", "COMPLETED"
        const val EXTRA_TEMPORAL_FILTER = "temporalFilter" // e.g., "TODAY", "TOMORROW"

        // Note: The RESULT_SUCCESS and RESULT_MESSAGE with LocalBroadcastManager
        // are for internal app communication. Gemini integration requires fulfilling the App Action.
        const val ACTION_ASSISTANT_RESULT = "com.zenithtasks.assistant.ASSISTANT_RESULT"
        const val EXTRA_RESULT_MESSAGE = "com.zenithtasks.assistant.EXTRA_RESULT_MESSAGE"
        const val EXTRA_RESULT_IS_SUCCESS = "com.zenithtasks.assistant.EXTRA_RESULT_IS_SUCCESS"

        /**
         * Register this receiver. For Gemini App Actions, this receiver should ideally be
         * registered in the AndroidManifest.xml with intent filters matching your actions.xml.
         * Dynamic registration with LocalBroadcastManager is more for internal app events.
         */
        fun register(context: Context): AssistantActionHandler {
            val receiver = AssistantActionHandler()
            val filter = IntentFilter().apply {
                addAction(ACTION_CREATE_TASK)
                addAction(ACTION_START_FOCUS)
                addAction(ACTION_GET_TASKS)
                addAction(ACTION_COMPLETE_TASK)
                // Add other actions from actions.xml
            }
            // Consider if LocalBroadcastManager is appropriate for your Gemini integration strategy.
            // If this receiver is manifest-declared for App Actions, this dynamic registration might be redundant or for other purposes.
            LocalBroadcastManager.getInstance(context).registerReceiver(receiver, filter)
            return receiver
        }

        fun unregister(context: Context, receiver: AssistantActionHandler) {
            LocalBroadcastManager.getInstance(context).unregisterReceiver(receiver)
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        // For App Actions, you might receive an AppActionToken to fulfill the action.
        // val appActionToken = intent.getParcelableExtra<AppActionToken>(AppActionToken.KEY_APP_ACTION_TOKEN)

        when (intent.action) {
            ACTION_CREATE_TASK -> {
                val taskName = intent.getStringExtra(EXTRA_TASK_NAME)
                if (taskName.isNullOrEmpty()) {
                    sendResponse(context, "Task name is required.", false)
                    return
                }
                val taskDescription = intent.getStringExtra(EXTRA_TASK_DESCRIPTION)
                val dueDateText = intent.getStringExtra(EXTRA_TASK_DUE_DATE_TEXT)
                // Potentially other parameters like priority, energy level if defined in actions.xml

                handleCreateTask(context, taskName, taskDescription, dueDateText, null, null)
            }

            ACTION_START_FOCUS -> {
                // BIIs for timers (e.g., actions.intent.SET_TIMER) have specific parameters like "duration" (ISO 8601)
                val durationMinutes = intent.getLongExtra(EXTRA_FOCUS_DURATION_MINUTES, 25L)
                handleStartFocus(context, durationMinutes)
            }

            ACTION_GET_TASKS -> {
                // Parameters for GET_TASK BII might include status, due date, etc.
                val filter = intent.getStringExtra(EXTRA_TEMPORAL_FILTER) ?: intent.getStringExtra(EXTRA_TASK_STATUS_FILTER)
                handleGetTasks(context, filter)
            }

            ACTION_COMPLETE_TASK -> {
                // Parameter could be task name or ideally a task ID if it can be resolved.
                val taskName = intent.getStringExtra(EXTRA_TASK_NAME) // Assuming task name is used to identify
                if (taskName.isNullOrEmpty()) {
                    sendResponse(context, "Task name to complete is required.", false)
                    return
                }
                handleCompleteTask(context, taskName)
            }
        }
        // After handling, you would fulfill the App Action, e.g., appActionToken.setSuccess("Action completed")
        // This part is crucial for Gemini and replaces the LocalBroadcastManager responses for external communication.
    }

    private fun parseDate(dateString: String?): Date? {
        if (dateString.isNullOrEmpty()) return null
        // Implement robust date parsing based on expected format from Assistant/Gemini
        // This is a very basic example
        return try {
            SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(dateString)
        } catch (e: Exception) {
            null // Or try other formats
        }
    }

    private fun handleCreateTask(
        context: Context,
        taskName: String,
        taskDescription: String?,
        dueDateString: String?,
        priorityString: String?, // These would come from intent extras if defined
        energyString: String?    // These would come from intent extras if defined
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val dueDate = parseDate(dueDateString)
                // Parse priority and energy level if provided and map to your enums
                val priority = priorityString?.let { 
                    try { TaskPriority.valueOf(it) } catch (e: Exception) { TaskPriority.MEDIUM } 
                } ?: TaskPriority.MEDIUM
                
                val energyLevel = energyString?.let { 
                    try { EnergyLevel.valueOf(it) } catch (e: Exception) { EnergyLevel.MEDIUM } 
                } ?: EnergyLevel.MEDIUM

                val task = Task(
                    id = 0, // Auto-generated by Room
                    title = taskName,
                    description = taskDescription ?: "",
                    dueDate = dueDate,
                    priority = priority, 
                    energyLevel = energyLevel,
                    isCompleted = false,
                    createdAt = Date(),
                    updatedAt = Date(),
                    reminder = null, // Requires dedicated parameter from Assistant
                    reminderTriggered = false,
                    locationId = null, // Requires dedicated parameter
                    locationReminderEnabled = false,
                    isArchived = false,
                    firebaseId = null, // Set if/when synced
                    lastSyncedAt = null,
                    pendingSync = true, // True for new tasks needing sync
                    completedDate = Date() // Use current date as completed date
                )

                taskRepository.addTask(task)
                sendResponse(context, "Task '$taskName' created successfully!", true)
                refreshWidgets(context)
            } catch (e: Exception) {
                sendResponse(context, "Failed to create task: ${e.message}", false)
            }
        }
    }

    private fun handleStartFocus(context: Context, durationMinutes: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentFocusState = focusRepository.focusState.first()
                if (currentFocusState is FocusState.Active) {
                    sendResponse(context, "Focus mode is already active. ${formatRemainingTime(currentFocusState)}", true)
                    return@launch
                }

                val serviceIntent = Intent(context, FocusControlService::class.java).apply {
                    action = FocusControlService.ACTION_START_FOCUS
                    putExtra(FocusControlService.EXTRA_DURATION_MINUTES, durationMinutes)
                }
                context.startService(serviceIntent)
                sendResponse(context, "Focus mode started for $durationMinutes minutes", true)
                refreshWidgets(context) // Assuming focus widget needs refresh
            } catch (e: Exception) {
                sendResponse(context, "Failed to start focus mode: ${e.message}", false)
            }
        }
    }

    private fun handleGetTasks(context: Context, filter: String?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = when (filter?.lowercase()) {
                    "today" -> taskRepository.getTodayTasks() // Assuming this method exists
                    "active" -> taskRepository.getIncompleteTasks() // Example
                    "completed" -> taskRepository.getCompletedTasks() // Example
                    // Add more filters based on BII parameters (priority, due date ranges etc.)
                    else -> taskRepository.getUpcomingTasks(5)
                }
                val taskSummary = formatTasksForVoice(tasks)
                // For GET_TASK BII, you'd fulfill with structured Task data, not just a string.
                sendResponse(context, taskSummary, true)
            } catch (e: Exception) {
                sendResponse(context, "Failed to get tasks: ${e.message}", false)
            }
        }
    }

    private fun handleCompleteTask(context: Context, taskName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val tasks = taskRepository.searchTasksByTitle(taskName) // Assumes case-insensitive partial match
                if (tasks.isEmpty()) {
                    sendResponse(context, "No task found matching '$taskName'", false)
                    return@launch
                }
                val taskToComplete = tasks.first() // Or handle multiple matches
                taskRepository.completeTask(taskToComplete.id, Date())
                sendResponse(context, "Task '${taskToComplete.title}' marked as complete", true)
                refreshWidgets(context)
            } catch (e: Exception) {
                sendResponse(context, "Failed to complete task: ${e.message}", false)
            }
        }
    }

    private fun formatTasksForVoice(tasks: List<Task>): String {
        if (tasks.isEmpty()) return "You don't have any tasks matching that."
        val sb = StringBuilder(if (tasks.size == 1) "You have one task: " else "Here are your tasks: ")
        tasks.take(5).forEachIndexed { index, task -> // Limit for voice
            if (index > 0) sb.append(if (index == tasks.size - 1 || index == 4) " and " else ", ")
            sb.append(task.title)
            // Optionally add due date or priority
        }
        if (tasks.size > 5) sb.append(" and some more.")
        return sb.toString()
    }

    private fun formatRemainingTime(focusState: FocusState.Active): String {
        // This calculation needs to be accurate based on startTime and duration
        val remaining = focusState.remainingTimeMillis / 60000 // Example conversion to minutes
        return if (remaining > 0) "$remaining minutes remaining" else "less than a minute remaining"
    }

    private fun refreshWidgets(context: Context) {
        // Ensure this action matches what your AppWidgetProvider listens for
        val refreshIntent = Intent("com.zenithtasks.widget.ACTION_REFRESH_WIDGETS")
            .setPackage(context.packageName) // Explicitly set package for broadcasts from API 26+
        context.sendBroadcast(refreshIntent)
    }

    /**
     * Sends a response via LocalBroadcastManager.
     * NOTE: For Gemini App Actions, you need to fulfill the action using the App Actions SDK
     * (e.g., by calling methods on an AppActionToken if available, or returning results from an Activity).
     * This LocalBroadcastManager mechanism is for internal app communication and won't directly
     * communicate success/failure/data back to Gemini.
     */
    private fun sendResponse(context: Context, message: String, isSuccess: Boolean) {
        val responseIntent = Intent(ACTION_ASSISTANT_RESULT).apply {
            putExtra(EXTRA_RESULT_MESSAGE, message)
            putExtra(EXTRA_RESULT_IS_SUCCESS, isSuccess)
        }
        LocalBroadcastManager.getInstance(context).sendBroadcast(responseIntent)

        // TODO: Implement proper App Action fulfillment here.
        // This might involve:
        // 1. If this BroadcastReceiver was started by an Activity that holds an AppActionToken,
        //    signal back to that Activity to call token.setSuccess() or token.setFailure().
        // 2. If handling a BII that returns data (like GET_TASK), you'd construct the result
        //    and use the token to provide it.
        // 3. If this is a fire-and-forget action, fulfillment might still be required to let
        //    Gemini know the action was received/attempted.
    }
}