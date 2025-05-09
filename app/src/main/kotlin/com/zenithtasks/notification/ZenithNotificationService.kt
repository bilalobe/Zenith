package com.zenithtasks.notification

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Person
import android.content.Context
import android.content.Intent
import android.content.pm.ShortcutInfo
import android.content.pm.ShortcutManager
import android.graphics.drawable.Icon
import androidx.core.app.NotificationCompat
import androidx.core.net.toUri
import com.zenithtasks.MainActivity
import com.zenithtasks.R
import com.zenithtasks.data.model.Task
import com.zenithtasks.ui.bubble.BubbleActivity
import dagger.hilt.android.qualifiers.ApplicationContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Service for handling all notification-related functionality in Zenith.
 * This includes task reminders, focus mode notifications, and bubbles.
 */
@Singleton
class ZenithNotificationService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val shortcutManager =
        context.getSystemService(ShortcutManager::class.java)

    companion object {
        // Notification channels
        const val CHANNEL_TASKS = "channel_tasks"
        const val CHANNEL_FOCUS = "channel_focus"
        const val CHANNEL_CHAT_BUBBLE = "channel_chat_bubble"
        
        // Notification IDs
        const val NOTIFICATION_ID_FOCUS = 1000
        
        // Intent actions
        const val ACTION_COMPLETE_TASK = "com.zenithtasks.notification.COMPLETE_TASK"
        const val ACTION_SNOOZE_TASK = "com.zenithtasks.notification.SNOOZE_TASK"
        const val EXTRA_TASK_ID = "com.zenithtasks.notification.EXTRA_TASK_ID"
        const val EXTRA_SNOOZE_DURATION = "com.zenithtasks.notification.EXTRA_SNOOZE_DURATION"
    }
    
    init {
        createNotificationChannels()
    }
    
    private fun createNotificationChannels() {
        // Task reminders channel
        val tasksChannel = NotificationChannel(
            CHANNEL_TASKS,
            "Task Reminders",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Notifications for task reminders and due dates"
            enableLights(true)
            enableVibration(true)
        }

        // Focus mode channel
        val focusChannel = NotificationChannel(
            CHANNEL_FOCUS,
            "Focus Mode",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Notifications related to focus mode status"
            enableLights(false)
            enableVibration(false)
        }

        // Chat bubbles channel
        val bubbleChannel = NotificationChannel(
            CHANNEL_CHAT_BUBBLE,
            "Task Bubbles",
            NotificationManager.IMPORTANCE_HIGH
        ).apply {
            description = "Conversation bubbles for interacting with tasks"
            setAllowBubbles(true)
        }

        // Register all channels
        notificationManager.createNotificationChannels(
            listOf(tasksChannel, focusChannel, bubbleChannel)
        )
    }
    
    /**
     * Show a task as a notification bubble that the user can interact with.
     * This creates a floating bubble on the screen that opens the task details when tapped.
     */
    fun showTaskAsBubble(task: Task) {

        // Create or update the dynamic shortcut for this task
        updateTaskShortcut(task)
        
        // Create a bubble notification
        val builder = NotificationCompat.Builder(context, CHANNEL_CHAT_BUBBLE)
            .setContentTitle(task.title)
            .setSmallIcon(R.drawable.ic_notification_task)
            .setCategory(Notification.CATEGORY_MESSAGE)
            .setShortcutId(getShortcutId(task))
            
        // Create bubble metadata
        val bubbleData = NotificationCompat.BubbleMetadata.Builder(
                createBubblePendingIntent(task),
            createTaskIcon(task)
            )
            .setDesiredHeight(600)
            .setAutoExpandBubble(task.priority.ordinal >= 1)  // Auto-expand for medium/high priority
            .setSuppressNotification(false)
            .build()
            
        // Add person for conversation style (required for bubbles)
        val zenithPerson = createZenithPerson()
        
        // Add bubble metadata and style to notification
        builder.setBubbleMetadata(bubbleData)
            .setStyle(NotificationCompat.MessagingStyle(zenithPerson)
                .addMessage(
                    NotificationCompat.MessagingStyle.Message(
                        getTaskReminderText(task),
                        System.currentTimeMillis(),
                        zenithPerson
                    )
                )
            )
            
        // Add actions
        addTaskActions(builder, task)
            
        // Show the notification
        notificationManager.notify(task.id.toInt(), builder.build())
    }
    
    /**
     * Show a regular task notification (for devices that don't support bubbles)
     */
    fun showTaskNotification(task: Task) {
        val builder = NotificationCompat.Builder(context, CHANNEL_TASKS)
            .setContentTitle(task.title)
            .setContentText(getTaskReminderText(task))
            .setSmallIcon(R.drawable.ic_notification_task)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER)
            .setContentIntent(createTaskContentIntent(task))
            .setAutoCancel(true)
            
        // Add actions
        addTaskActions(builder, task)
        
        // Show the notification
        notificationManager.notify(task.id.toInt(), builder.build())
    }
    
    /**
     * Add common actions to task notifications
     */
    private fun addTaskActions(builder: NotificationCompat.Builder, task: Task) {
        // Complete action
        val completeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_COMPLETE_TASK
            putExtra(EXTRA_TASK_ID, task.id)
        }
        val completePendingIntent = PendingIntent.getBroadcast(
            context,
            task.id.toInt(),
            completeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_check,
            "Complete",
            completePendingIntent
        )
        
        // Snooze action
        val snoozeIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_SNOOZE_TASK
            putExtra(EXTRA_TASK_ID, task.id)
            putExtra(EXTRA_SNOOZE_DURATION, 30L) // 30 minutes by default
        }
        val snoozePendingIntent = PendingIntent.getBroadcast(
            context,
            (task.id + 1000).toInt(), // Ensure unique request code
            snoozeIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        builder.addAction(
            R.drawable.ic_snooze,
            "Snooze",
            snoozePendingIntent
        )
    }
    
    /**
     * Create a PendingIntent for opening the bubble activity
     */
    private fun createBubblePendingIntent(task: Task): PendingIntent {
        val intent = Intent(context, BubbleActivity::class.java).apply {
            putExtra(BubbleActivity.EXTRA_TASK_ID, task.id)
            
            // Add these flags to specify the activity should be launched in a new document/task
            flags = Intent.FLAG_ACTIVITY_NEW_DOCUMENT or Intent.FLAG_ACTIVITY_CLEAR_TASK
            
            // Create a unique data URI for this specific task bubble
            data = "zenith://task/${task.id}".toUri()
        }
        
        return PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }
    
    /**
     * Create a PendingIntent for opening the task in the main app
     */
    private fun createTaskContentIntent(task: Task): PendingIntent {
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("EXTRA_OPEN_TASK_DETAIL", true)
            putExtra("EXTRA_TASK_ID", task.id)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        
        return PendingIntent.getActivity(
            context,
            task.id.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }
    
    /**
     * Create a Person object to represent Zenith in the conversation bubble
     */
    private fun createZenithPerson(): androidx.core.app.Person {
        return androidx.core.app.Person.Builder()
            .setName("Zenith")
            .setIcon(androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.mipmap.ic_launcher_round))
            .setBot(true)
            .build()
    }
    
    /**
     * Create an icon for the task bubble
     */
    private fun createTaskIcon(task: Task): androidx.core.graphics.drawable.IconCompat {
        // For a real app, you might use different icons based on task priority or category
        // or even user avatars if the tasks are assigned by different people
        return androidx.core.graphics.drawable.IconCompat.createWithResource(context, R.mipmap.ic_launcher_round)
    }
    
    /**
     * Create or update a shortcut for the task (required for bubbles)
     */
    private fun updateTaskShortcut(task: Task) {
        val shortcutId = getShortcutId(task)
        
        val taskIcon = Icon.createWithResource(context, R.drawable.ic_notification_task)
        
        val shortcutIntent = Intent(context, BubbleActivity::class.java).apply {
            action = Intent.ACTION_VIEW
            putExtra(BubbleActivity.EXTRA_TASK_ID, task.id)
            data = "zenith://task/${task.id}".toUri()
        }
        
        val shortcut = ShortcutInfo.Builder(context, shortcutId)
            .setShortLabel(task.title)
            .setLongLabel(task.title)
            .setIcon(taskIcon)
            .setIntent(shortcutIntent)
            .setPerson(
                Person.Builder()
                    .setName("Zenith")
                    .build()
            )
            .build()
            
        shortcutManager?.addDynamicShortcuts(listOf(shortcut))
    }
    
    /**
     * Generate a consistent shortcut ID for a task
     */
    private fun getShortcutId(task: Task): String {
        return "task_${task.id}"
    }
    
    /**
     * Generate appropriate reminder text for a task based on its due date
     */
    private fun getTaskReminderText(task: Task): String {
        // Get appropriate message based on due date
        return task.dueDate?.let { dueDate ->
            val now = Date()
            when {
                dueDate.before(now) -> "This task is overdue! It was due on ${formatDate(dueDate)}"
                isSameDay(now, dueDate) -> "This task is due today!"
                else -> "This task is due on ${formatDate(dueDate)}"
            }
        } ?: "This task needs your attention"
    }
    
    /**
     * Format a date for display in notifications
     */
    private fun formatDate(date: Date): String {
        // In a real app, use DateFormat with appropriate localization
        return date.toString()
    }
    
    /**
     * Check if two dates are on the same day
     */
    private fun isSameDay(date1: Date, date2: Date): Boolean {
        // Simple check - in a real app use Calendar or LocalDate
        return date1.toString().substring(0, 10) == date2.toString().substring(0, 10)
    }
}