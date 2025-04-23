package com.zenithtasks.location

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.zenithtasks.MainActivity
import com.zenithtasks.R

/**
 * Helper class for creating and showing notifications.
 */
class NotificationHelper(private val context: Context) {

    private val TAG = "NotificationHelper"
    private val CHANNEL_ID = "task_reminder_channel"

    init {
        createNotificationChannel()
    }

    /**
     * Creates the notification channel for task reminders.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Task Reminders"
            val descriptionText = "Notifications for task reminders based on location"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Shows a notification for a task at a location.
     *
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param locationName The name of the location
     */
    fun showTaskNotification(taskId: Long, taskTitle: String, locationName: String) {
        // Create intent to open the task details
        val intent = Intent(context, MainActivity::class.java).apply {
            putExtra("taskId", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            context, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setContentTitle("Task Reminder: $taskTitle")
            .setContentText("You're at $locationName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Check for notification permission on Android 13+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ActivityCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                Log.e(TAG, "POST_NOTIFICATIONS permission not granted")
                return
            }
        }

        // Show the notification
        NotificationManagerCompat.from(context).notify(taskId.toInt() + 100, notification)
    }
}