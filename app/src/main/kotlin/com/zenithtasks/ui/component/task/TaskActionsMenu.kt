package com.zenithtasks.ui.component.task

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.zenithtasks.data.model.Task

/**
 * A dropdown menu for task actions like snoozing
 */
@Composable
fun TaskActionsMenu(
    task: Task,
    onSnoozeClick: () -> Unit = {},
    onUnsnoozeClick: () -> Unit = {}
) {
    var expanded by remember { mutableStateOf(false) }
    
    IconButton(onClick = { expanded = true }) {
        Icon(
            imageVector = Icons.Default.MoreVert,
            contentDescription = "Task Options"
        )
        
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            if (task.isSnoozed) {
                // Option to un-snooze a snoozed task
                DropdownMenuItem(
                    text = { Text("Un-snooze") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.AlarmOff,
                            contentDescription = "Un-snooze task"
                        )
                    },
                    onClick = {
                        expanded = false
                        onUnsnoozeClick()
                    }
                )
            } else {
                // Option to snooze a task
                DropdownMenuItem(
                    text = { Text("Snooze") },
                    leadingIcon = { 
                        Icon(
                            imageVector = Icons.Default.Snooze,
                            contentDescription = "Snooze task"
                        )
                    },
                    onClick = {
                        expanded = false
                        onSnoozeClick()
                    }
                )
            }
        }
    }
}