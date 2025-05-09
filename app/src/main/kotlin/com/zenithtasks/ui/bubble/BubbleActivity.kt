package com.zenithtasks.ui.bubble

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.zenithtasks.ui.component.task.detail.TaskDetailBody
import com.zenithtasks.ui.theme.ZenithTheme
import com.zenithtasks.ui.viewmodel.TaskDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

/**
 * Activity shown in a bubble when a user interacts with a task notification bubble.
 */
@AndroidEntryPoint
class BubbleActivity : ComponentActivity() {
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Get task ID from intent
        val taskId = intent.getLongExtra(EXTRA_TASK_ID, -1L)
        if (taskId == -1L) {
            finish()
            return
        }
        
        setContent {
            ZenithTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    TaskBubbleContent(
                        taskId = taskId,
                        onComplete = {
                            // Send broadcast to update widgets and other UI
                            sendBroadcast(Intent(ACTION_TASK_COMPLETED).apply {
                                putExtra(EXTRA_TASK_ID, taskId)
                            })
                            finish()
                        },
                        onSnooze = { durationMinutes ->
                            // Send broadcast to snooze the task
                            sendBroadcast(Intent(ACTION_TASK_SNOOZED).apply {
                                putExtra(EXTRA_TASK_ID, taskId)
                                putExtra(EXTRA_SNOOZE_DURATION, durationMinutes)
                            })
                            finish()
                        },
                        onClose = {
                            finish()
                        }
                    )
                }
            }
        }
    }
    
    companion object {
        const val EXTRA_TASK_ID = "com.zenithtasks.ui.bubble.EXTRA_TASK_ID"
        const val EXTRA_SNOOZE_DURATION = "com.zenithtasks.ui.bubble.EXTRA_SNOOZE_DURATION"
        const val ACTION_TASK_COMPLETED = "com.zenithtasks.ui.bubble.ACTION_TASK_COMPLETED"
        const val ACTION_TASK_SNOOZED = "com.zenithtasks.ui.bubble.ACTION_TASK_SNOOZED"
    }
}

@Composable
fun TaskBubbleContent(
    taskId: Long,
    viewModel: TaskDetailViewModel = viewModel(),
    onComplete: () -> Unit,
    onSnooze: (Long) -> Unit,
    onClose: () -> Unit
) {
    // Load task details
    viewModel.loadTask(taskId)
    val task by viewModel.task.collectAsState(null)
    
    task?.let { taskData ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Task details
            TaskDetailBody(
                task = taskData,
                isLoading = TODO(),
                paddingValues = TODO()
            )
            
            // Quick action buttons
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(8.dp)
            ) {
                TextButton(
                    onClick = onComplete,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Mark as Complete")
                }
                
                TextButton(
                    onClick = { onSnooze(30) },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Snooze (30 min)")
                }
                
                TextButton(
                    onClick = { onSnooze(60) },
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Snooze (1 hour)")
                }
                
                TextButton(
                    onClick = onClose,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Text("Close")
                }
            }
        }
    } ?: run {
        // Show loading or error state
        Text(
            text = "Loading task details...",
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            style = MaterialTheme.typography.bodyLarge
        )
    }
}