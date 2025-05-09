package com.zenithtasks.ui.component.task

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.Task
import com.zenithtasks.data.model.TaskPriority
import com.zenithtasks.ui.component.dialog.SnoozeIndicator
import com.zenithtasks.ui.component.energy.EnergyLevelChip
import java.time.ZoneId
import java.time.format.DateTimeFormatter

@Composable
fun TaskItem(
    task: Task,
    onTaskClick: (Task) -> Unit = {},
    onTaskCheckChanged: (Task, Boolean) -> Unit = { _, _ -> },
    onTaskLongPress: (Task) -> Unit = {},
    onSnoozeClick: (Task) -> Unit = {},
    onUnsnoozeClick: (Task) -> Unit = {}
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp)
            .clickable { onTaskClick(task) }
            .pointerInput(Unit) {
                detectTapGestures(
                    onLongPress = { onTaskLongPress(task) }
                )
            },
        shape = RoundedCornerShape(12.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
        // Apply opacity if task is snoozed
        colors = if (task.isSnoozed) {
            CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.7f)
            )
        } else {
            CardDefaults.cardColors()
        }
    ) {
        Column {
            // Show snooze indicator if task is snoozed
            if (task.isSnoozed && task.snoozeUntil != null) {
                SnoozeIndicator(
                    snoozeUntil = task.snoozeUntil,
                    onUnsnooze = { onUnsnoozeClick(task) }
                )
            }
            
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Checkbox for task completion
                Checkbox(
                    checked = task.isCompleted,
                    onCheckedChange = { isChecked -> onTaskCheckChanged(task, isChecked) },
                    modifier = Modifier.padding(end = 8.dp)
                )
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            when (task.priority) {
                                TaskPriority.HIGH -> Color(0xFFF44336)
                                TaskPriority.MEDIUM -> Color(0xFFFF9800)
                                else -> Color(0xFF4CAF50)
                            },
                            shape = CircleShape
                        )
                )

                Spacer(modifier = Modifier.width(16.dp))

                Column(
                    modifier = Modifier.weight(1f)
                ) {
                    Text(
                        text = task.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        textDecoration = if (task.isCompleted) TextDecoration.LineThrough else TextDecoration.None
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    if (!task.description.isNullOrBlank()) {
                        Text(
                            text = task.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        EnergyLevelChip(energyLevel = task.energyLevel)

                        Spacer(modifier = Modifier.width(8.dp))

                        task.dueDate?.let { dueDate ->
                            val formattedDate = remember(dueDate) {
                                val formatter = DateTimeFormatter.ofPattern("MMM d")
                                val instant = dueDate.toInstant()
                                val localDate = instant.atZone(ZoneId.systemDefault()).toLocalDate()
                                localDate.format(formatter)
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DateRange,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp),
                                    tint = MaterialTheme.colorScheme.primary
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = formattedDate,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
                
                // Task actions menu
                TaskActionsMenu(
                    task = task, 
                    onSnoozeClick = { onSnoozeClick(task) },
                    onUnsnoozeClick = { onUnsnoozeClick(task) }
                )
            }
        }
    }
}
