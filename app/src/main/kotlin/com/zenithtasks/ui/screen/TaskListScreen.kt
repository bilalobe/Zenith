package com.zenithtasks.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FabPosition
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.Task
import com.zenithtasks.ui.component.common.AdaptiveHeader
import com.zenithtasks.ui.component.common.SearchBar
import com.zenithtasks.ui.component.dialog.AddTaskDialog
import com.zenithtasks.ui.component.dialog.SnoozeTaskDialog
import com.zenithtasks.ui.component.task.TaskList
import com.zenithtasks.ui.viewmodel.TaskListViewModel

/**
 * Main screen for displaying and managing tasks.
 */
@Composable
fun TaskListScreen(
    viewModel: TaskListViewModel,
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToAddTask: () -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val filteredTasks by viewModel.filteredTasks.collectAsState()
    var showAddTaskDialog by remember { mutableStateOf(false) }
    
    // Snooze-related states
    var showSnoozeDialog by remember { mutableStateOf(false) }
    var taskToSnooze by remember { mutableStateOf<Task?>(null) }
    
    Scaffold(
        topBar = {
            AdaptiveHeader(title = "Tasks")
        },
        floatingActionButton = {
            FloatingActionButton(onClick = { showAddTaskDialog = true }) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        },
        floatingActionButtonPosition = FabPosition.End
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            SearchBar(
                query = searchQuery,
                onQueryChange = { viewModel.updateSearchQuery(it) }
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            TaskList(
                tasks = filteredTasks,
                onTaskCompleted = { task -> viewModel.completeTask(task) },
                onTaskDeleted = { task -> viewModel.deleteTask(task) },
                onTaskArchived = { /* viewModel.archiveTask(it) */ }, // Implement if needed
                onTaskClick = { task -> onNavigateToTaskDetail(task.id) },
                onTaskPriorityChanged = { task, newPriority ->
                    viewModel.updateTaskPriority(task, newPriority)
                },
                onTaskSnooze = { task ->
                    taskToSnooze = task
                    showSnoozeDialog = true
                },
                onTaskUnsnooze = { task ->
                    viewModel.unsnoozeTask(task)
                }
            )
        }
    }
    
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskCreated = { task ->
                viewModel.addTask(task)
                showAddTaskDialog = false
            },
            energyLevel = TODO()
        )
    }
    
    // Show SnoozeTaskDialog when needed
    if (showSnoozeDialog && taskToSnooze != null) {
        SnoozeTaskDialog(
            task = taskToSnooze!!,
            onDismiss = { showSnoozeDialog = false },
            onSnooze = { task, option ->
                viewModel.snoozeTask(task, option)
                showSnoozeDialog = false
            },
            onSnoozeUntilDate = { task, date ->
                viewModel.snoozeTaskUntil(task, date)
                showSnoozeDialog = false
            }
        )
    }
}