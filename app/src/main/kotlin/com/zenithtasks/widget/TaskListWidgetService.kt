package com.zenithtasks.widget

import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import androidx.core.graphics.toColorInt
import com.zenithtasks.R
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskPriority
import com.zenithtasks.domain.repository.TaskRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.runBlocking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import javax.inject.Inject

/**
 * Service for providing remote views for the tasks list widget.
 */
@AndroidEntryPoint
class TaskListWidgetService : RemoteViewsService() {
    
    @Inject
    lateinit var taskRepository: TaskRepository
    
    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return TaskListRemoteViewsFactory(this.applicationContext, taskRepository)
    }
}

/**
 * Factory that creates remote views for the task list widget.
 */
class TaskListRemoteViewsFactory(
    private val context: Context,
    private val taskRepository: TaskRepository
) : RemoteViewsService.RemoteViewsFactory {
    
    private var tasks: List<Task> = emptyList()
    
    override fun onCreate() {
        // Nothing to do here
    }
    
    override fun onDataSetChanged() {
        // This is called when notifyAppWidgetViewDataChanged is called
        runBlocking {
            tasks = taskRepository.getActiveTasksForWidget().firstOrNull() ?: emptyList()
        }
    }
    
    override fun onDestroy() {
        tasks = emptyList()
    }
    
    override fun getCount(): Int = tasks.size
    
    override fun getViewAt(position: Int): RemoteViews {
        if (position < 0 || position >= tasks.size) {
            return RemoteViews(context.packageName, R.layout.widget_task_item)
        }
        
        val task = tasks[position]
        
        return RemoteViews(context.packageName, R.layout.widget_task_item).apply {
            // Set task title
            setTextViewText(R.id.text_task_title, task.title)
            
            // Set due date text
            val dueText = task.dueDate?.let {
                "Due: ${formatDueDate(it)}"
            } ?: "No due date"
            setTextViewText(R.id.text_task_due, dueText)
            
            // Set priority indicator color
            val priorityColor = when (task.priority) {
                TaskPriority.HIGH -> "#F44336".toColorInt()
                TaskPriority.MEDIUM -> "#FF9800".toColorInt()
                TaskPriority.LOW -> "#4CAF50".toColorInt()
            }
            setInt(R.id.image_priority, "setBackgroundColor", priorityColor)
            
            // Fill in the pending intent template for item clicks
            val fillInIntent = Intent().apply {
                putExtra(TaskListWidgetProvider.EXTRA_TASK_ID, task.id)
            }
            setOnClickFillInIntent(R.id.text_task_title, fillInIntent)
            
            // Create a separate intent for the checkbox to complete the task
            val completeIntent = Intent().apply {
                action = TaskListWidgetProvider.ACTION_COMPLETE_TASK
                putExtra(TaskListWidgetProvider.EXTRA_TASK_ID, task.id)
            }
            setOnClickFillInIntent(R.id.checkbox_task, completeIntent)
        }
    }
    
    override fun getLoadingView(): RemoteViews {
        return RemoteViews(context.packageName, R.layout.widget_task_item)
    }
    
    override fun getViewTypeCount(): Int = 1
    
    override fun getItemId(position: Int): Long {
        return if (position < tasks.size) tasks[position].id else position.toLong()
    }
    
    override fun hasStableIds(): Boolean = true
    
    private fun formatDueDate(date: Date): String {
        val today = Date()
        val tomorrow = Date(today.time + 24 * 60 * 60 * 1000)
        
        val todayString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(today)
        val tomorrowString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(tomorrow)
        val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(date)
        
        return when (dateString) {
            todayString -> "Today"
            tomorrowString -> "Tomorrow"
            else -> SimpleDateFormat("MMM d", Locale.getDefault()).format(date)
        }
    }
}