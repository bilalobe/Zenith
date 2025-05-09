package com.zenithtasks.ui.component.task

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskPriority
import com.zenithtasks.ui.component.dialog.PriorityChangeDialog

@Composable
fun TaskList(
    tasks: List<Task>,
    onTaskCompleted: (Task) -> Unit = {},
    onTaskDeleted: (Task) -> Unit = {},
    onTaskArchived: (Task) -> Unit = {},
    onTaskClick: (Task) -> Unit = {},
    onTaskPriorityChanged: (Task, TaskPriority) -> Unit = { _, _ -> },
    onTaskSnooze: (Task) -> Unit = {},
    onTaskUnsnooze: (Task) -> Unit = {}
) {
    var showPriorityDialog by remember { mutableStateOf(false) }
    var selectedTask by remember { mutableStateOf<Task?>(null) }

    if (tasks.isEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            shape = RoundedCornerShape(12.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Text(
                text = "No tasks found for your current energy level",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(16.dp)
            )
        }
    } else {
        LazyColumn(
            state = rememberLazyListState()
        ) {
            items(
                items = tasks,
                key = { task -> task.id }
            ) { task ->
                TaskItem(
                    task = task,
                    onTaskClick = { onTaskClick(task) },
                    onTaskCheckChanged = { taskItem, isChecked ->
                        if (isChecked) {
                            onTaskCompleted(taskItem)
                        }
                    },
                    onTaskLongPress = { 
                        selectedTask = it
                        showPriorityDialog = true
                    },
                    onSnoozeClick = { onTaskSnooze(task) },
                    onUnsnoozeClick = { onTaskUnsnooze(task) }
                )
            }
        }

        // Show priority change dialog when a task is long-pressed
        selectedTask?.let { task ->
            if (showPriorityDialog) {
                PriorityChangeDialog(
                    task = task,
                    onDismiss = { showPriorityDialog = false },
                    onPriorityChanged = { changedTask, newPriority ->
                        onTaskPriorityChanged(changedTask, newPriority)
                    }
                )
            }
        }
    }
}
