package com.zenithtasks.ui.component.dialog

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AlarmOff
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Snooze
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.SnoozeOptions
import com.zenithtasks.data.model.Task
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId
import java.util.Date
import java.util.Locale

/**
 * A dialog that allows users to choose when to snooze a task
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SnoozeTaskDialog(
    task: Task,
    onDismiss: () -> Unit,
    onSnooze: (Task, SnoozeOptions) -> Unit,
    onSnoozeUntilDate: (Task, Date) -> Unit
) {
    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }
    
    var selectedDate by remember { mutableStateOf<Long?>(null) }
    var selectedTime by remember { 
        mutableStateOf(
            LocalTime.of(
                LocalDateTime.now().hour + 1, 
                0
            )
        ) 
    }

    // Show the main snooze options dialog
    if (!showDatePicker && !showTimePicker) {
        AlertDialog(
            onDismissRequest = onDismiss,
            icon = { Icon(Icons.Default.Snooze, contentDescription = null) },
            title = { Text("Snooze Task") },
            text = {
                Column {
                    Text(
                        "Hide \"${task.title}\" until later?",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    Spacer(modifier = Modifier.height(16.dp))

                    // Predefined snooze options
                    SnoozeOptions.entries.forEach { option ->
                        if (option != SnoozeOptions.CUSTOM) {
                            ListItem(
                                headlineContent = { Text(option.displayName) },
                                modifier = Modifier.clickable {
                                    onSnooze(task, option)
                                    onDismiss()
                                }
                            )
                        }
                    }
                    
                    // Custom option with date picker
                    ListItem(
                        headlineContent = { Text(SnoozeOptions.CUSTOM.displayName) },
                        leadingContent = { 
                            Icon(
                                imageVector = Icons.Default.DateRange,
                                contentDescription = "Choose date"
                            )
                        },
                        modifier = Modifier.clickable {
                            showDatePicker = true
                        }
                    )
                }
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = onDismiss) {
                    Text("Cancel")
                }
            }
        )
    }
    
    // Date picker dialog
    if (showDatePicker) {
        val datePickerState = rememberDatePickerState()
        
        DatePickerDialog(
            onDismissRequest = {
                showDatePicker = false
            },
            confirmButton = {
                TextButton(onClick = {
                    selectedDate = datePickerState.selectedDateMillis
                    showDatePicker = false
                    showTimePicker = true
                }) {
                    Text("Next")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDatePicker = false
                }) {
                    Text("Cancel")
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
    
    // Time picker dialog
    if (showTimePicker && selectedDate != null) {
        val now = LocalTime.now()
        val timePickerState = rememberTimePickerState(
            initialHour = now.hour + 1,
            initialMinute = 0
        )
        
        AlertDialog(
            onDismissRequest = {
                showTimePicker = false
            },
            title = { Text("Select Time") },
            text = {
                Column(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                ) {
                    TimePicker(state = timePickerState)
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        // Create Date from selected date and time
                        val selectedDateTime = LocalDateTime.ofInstant(
                            Instant.ofEpochMilli(selectedDate!!),
                            ZoneId.systemDefault()
                        ).withHour(timePickerState.hour)
                         .withMinute(timePickerState.minute)
                         .withSecond(0)
                        
                        val snoozeDate = Date.from(
                            selectedDateTime.atZone(ZoneId.systemDefault()).toInstant()
                        )
                        
                        onSnoozeUntilDate(task, snoozeDate)
                        showTimePicker = false
                        onDismiss()
                    }
                ) {
                    Text("Set Snooze")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showTimePicker = false
                    showDatePicker = true
                }) {
                    Text("Back")
                }
            }
        )
    }
}

/**
 * A small bubble showing that a task is snoozed
 */
@Composable
fun SnoozeIndicator(
    snoozeUntil: Date,
    onUnsnooze: () -> Unit = {}
) {
    val formatter = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }
    val formattedDate = remember(snoozeUntil) { formatter.format(snoozeUntil) }
    
    Row(
        modifier = Modifier
            .clickable { onUnsnooze() }
            .padding(4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.AlarmOff,
            contentDescription = "Snoozed",
            tint = MaterialTheme.colorScheme.primary
        )
        Text(
            text = "Snoozed until $formattedDate",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(start = 4.dp)
        )
    }
}