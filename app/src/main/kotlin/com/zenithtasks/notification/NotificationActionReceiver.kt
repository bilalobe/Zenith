package com.zenithtasks.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.zenithtasks.domain.repository.TaskRepository
import com.zenithtasks.widget.TaskListWidgetProvider
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

/**
 * Handles notification action intents for completing and snoozing tasks
 */
@AndroidEntryPoint
class NotificationActionReceiver : BroadcastReceiver() {
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ZenithNotificationService.ACTION_COMPLETE_TASK -> {
                val taskId = intent.getLongExtra(ZenithNotificationService.EXTRA_TASK_ID, -1L)
                if (taskId != -1L) {
                    handleCompleteTask(context, taskId)
                }
            }
            
            ZenithNotificationService.ACTION_SNOOZE_TASK -> {
                val taskId = intent.getLongExtra(ZenithNotificationService.EXTRA_TASK_ID, -1L)
                val durationMinutes = intent.getLongExtra(ZenithNotificationService.EXTRA_SNOOZE_DURATION, 30L)
                if (taskId != -1L) {
                    handleSnoozeTask(context, taskId, durationMinutes)
                }
            }
        }
    }
    
    private fun handleCompleteTask(context: Context, taskId: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // Mark the task as complete in the repository
            taskRepository.completeTask(taskId, Date())
            
            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(taskId.toInt())
            
            // Refresh widgets
            TaskListWidgetProvider.refreshWidgets(context)
        }
    }
    
    private fun handleSnoozeTask(context: Context, taskId: Long, durationMinutes: Long) {
        CoroutineScope(Dispatchers.IO).launch {
            // Snooze the task in the repository
            taskRepository.snoozeTask(taskId, durationMinutes)
            
            // Cancel the notification
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            notificationManager.cancel(taskId.toInt())
        }
    }
}