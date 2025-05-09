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
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskPriority
import com.zenithtasks.ui.component.energy.EnergyChoice
import com.zenithtasks.ui.component.priority.PriorityChoice

@Composable
fun AddTaskDialog(
    onDismiss: () -> Unit,
    onTaskCreated: (Task) -> Unit,
    energyLevel: EnergyLevel
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedPriority by remember { mutableStateOf(TaskPriority.MEDIUM) }
    var selectedEnergyLevel by remember { mutableStateOf(energyLevel) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add New Task") },
        text = {
            Column(modifier = Modifier.padding(8.dp)) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Title") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                OutlinedTextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(100.dp)
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Priority", style = MaterialTheme.typography.labelLarge)
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
                
                Spacer(modifier = Modifier.height(8.dp))
                
                Text("Energy Level", style = MaterialTheme.typography.labelLarge)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceAround
                ) {
                    EnergyChoice(
                        energyLevel = EnergyLevel.LOW,
                        isSelected = selectedEnergyLevel == EnergyLevel.LOW,
                        onClick = { selectedEnergyLevel = EnergyLevel.LOW }
                    )
                    EnergyChoice(
                        energyLevel = EnergyLevel.MEDIUM,
                        isSelected = selectedEnergyLevel == EnergyLevel.MEDIUM,
                        onClick = { selectedEnergyLevel = EnergyLevel.MEDIUM }
                    )
                    EnergyChoice(
                        energyLevel = EnergyLevel.HIGH,
                        isSelected = selectedEnergyLevel == EnergyLevel.HIGH,
                        onClick = { selectedEnergyLevel = EnergyLevel.HIGH }
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (title.isNotBlank()) {
                        onTaskCreated(
                            Task(
                                id = 0, // Repository will assign real ID
                                title = title,
                                description = description,
                                priority = selectedPriority,
                                energyLevel = selectedEnergyLevel,
                                isCompleted = false,
                                isArchived = false
                            )
                        )
                    }
                },
                enabled = title.isNotBlank()
            ) {
                Text("Add Task")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
