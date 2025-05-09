package com.zenithtasks.widget

import android.annotation.SuppressLint
import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import androidx.core.net.toUri
import com.zenithtasks.MainActivity
import com.zenithtasks.R
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Implementation of App Widget functionality for the Task List widget.
 * Shows a list of upcoming tasks directly on the home screen.
 */
@AndroidEntryPoint
class TaskListWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var widgetTaskRepository: WidgetTaskRepository

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        // There may be multiple widgets active, so update all of them
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onEnabled(context: Context) {
        // Enter relevant functionality for when the first widget is created
    }

    override fun onDisabled(context: Context) {
        // Enter relevant functionality for when the last widget is disabled
    }

    @Suppress("DEPRECATION")
    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        if (intent.action == ACTION_REFRESH_WIDGET) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, TaskListWidgetProvider::class.java)
            )
            // This is deprecated but still works fine
            appWidgetIds.forEach { widgetId ->
                appWidgetManager.notifyAppWidgetViewDataChanged(widgetId, R.id.list_tasks)
            }
            
            // Update all widgets
            onUpdate(context, appWidgetManager, appWidgetIds)
        } else if (intent.action == ACTION_TASK_CLICKED) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            if (taskId != -1L) {
                val mainIntent = Intent(context, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra(EXTRA_OPEN_TASK_DETAIL, true)
                    putExtra(EXTRA_TASK_ID, taskId)
                }
                context.startActivity(mainIntent)
            }
        } else if (intent.action == ACTION_COMPLETE_TASK) {
            val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
            if (taskId != -1L) {
                CoroutineScope(Dispatchers.IO).launch {
                    widgetTaskRepository.completeTask(taskId)
                    
                    // Refresh the widget
                    val refreshIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
                        action = ACTION_REFRESH_WIDGET
                    }
                    context.sendBroadcast(refreshIntent)
                }
            }
        } else if (intent.action == ACTION_ADD_TASK) {
            val mainIntent = Intent(context, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                putExtra(EXTRA_OPEN_ADD_TASK, true)
            }
            context.startActivity(mainIntent)
        }
    }

    companion object {
        const val ACTION_REFRESH_WIDGET = "com.zenithtasks.widget.ACTION_REFRESH_WIDGET"
        const val ACTION_TASK_CLICKED = "com.zenithtasks.widget.ACTION_TASK_CLICKED"
        const val ACTION_COMPLETE_TASK = "com.zenithtasks.widget.ACTION_COMPLETE_TASK"
        const val ACTION_ADD_TASK = "com.zenithtasks.widget.ACTION_ADD_TASK"
        const val EXTRA_TASK_ID = "com.zenithtasks.widget.EXTRA_TASK_ID"
        const val EXTRA_OPEN_TASK_DETAIL = "com.zenithtasks.widget.EXTRA_OPEN_TASK_DETAIL"
        const val EXTRA_OPEN_ADD_TASK = "com.zenithtasks.widget.EXTRA_OPEN_ADD_TASK"

        /**
         * Trigger an update for all instances of the Task List Widget
         */
        fun refreshWidgets(context: Context) {
            val intent = Intent(context, TaskListWidgetProvider::class.java).apply {
                action = ACTION_REFRESH_WIDGET
            }
            context.sendBroadcast(intent)
        }
    }

    @SuppressLint("RemoteViewLayout")
    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_task_list)

        // Set up the intent that starts the TaskListWidgetService, which provides
        // the views for this collection.
        val intent = Intent(context, TaskListWidgetService::class.java).apply {
            putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            data = toUri(Intent.URI_INTENT_SCHEME).toUri()
        }

        @Suppress("DEPRECATION")
        views.setRemoteAdapter(R.id.list_tasks, intent)

        // Set up empty view
        views.setEmptyView(R.id.list_tasks, R.id.empty_view)

        // Template to handle the list item click
        val taskClickIntentTemplate = Intent(context, TaskListWidgetProvider::class.java).apply {
            action = ACTION_TASK_CLICKED
        }
        val taskClickPendingIntent = PendingIntent.getBroadcast(
            context, 0, taskClickIntentTemplate, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setPendingIntentTemplate(R.id.list_tasks, taskClickPendingIntent)

        // Handle refresh button click
        val refreshIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
            action = ACTION_REFRESH_WIDGET
        }
        val refreshPendingIntent = PendingIntent.getBroadcast(
            context, 0, refreshIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_refresh, refreshPendingIntent)

        // Handle add task button click
        val addTaskIntent = Intent(context, TaskListWidgetProvider::class.java).apply {
            action = ACTION_ADD_TASK
        }
        val addTaskPendingIntent = PendingIntent.getBroadcast(
            context, 0, addTaskIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
        views.setOnClickPendingIntent(R.id.btn_add_task, addTaskPendingIntent)

        // Tell the AppWidgetManager to perform an update on the current app widget
        appWidgetManager.updateAppWidget(appWidgetId, views)
    }
}