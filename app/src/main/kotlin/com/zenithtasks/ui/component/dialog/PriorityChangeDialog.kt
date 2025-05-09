package com.zenithtasks.ui.component.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskPriority
import com.zenithtasks.ui.component.priority.PriorityChoice

@Composable
fun PriorityChangeDialog(
    task: Task,
    onDismiss: () -> Unit,
    onPriorityChanged: (Task, TaskPriority) -> Unit
) {
    var selectedPriority by remember { mutableStateOf(task.priority) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Change Task Priority") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                Text("Task: ${task.title}", style = MaterialTheme.typography.bodyLarge)
                
                Spacer(modifier = Modifier.height(16.dp))
                
                Text("Select Priority", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    PriorityChoice(
                        priority = TaskPriority.LOW,
                        isSelected = selectedPriority == TaskPriority.LOW,
                        onClick = { selectedPriority = TaskPriority.LOW }
                    )
                    PriorityChoice(
                        priority = TaskPriority.MEDIUM,
                        isSelected = selectedPriority == TaskPriority.MEDIUM,
                        onClick = { selectedPriority = TaskPriority.MEDIUM }
                    )
                    PriorityChoice(
                        priority = TaskPriority.HIGH,
                        isSelected = selectedPriority == TaskPriority.HIGH,
                        onClick = { selectedPriority = TaskPriority.HIGH }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPriorityChanged(task, selectedPriority)
                    onDismiss()
                }
            ) {
                Text("Save")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}