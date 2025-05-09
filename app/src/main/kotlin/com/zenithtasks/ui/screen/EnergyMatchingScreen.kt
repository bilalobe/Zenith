package com.zenithtasks.ui.screen

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithtasks.ui.component.common.AdaptiveHeader
import com.zenithtasks.ui.component.common.SortMenu
import com.zenithtasks.ui.component.dialog.AddTaskDialog
import com.zenithtasks.ui.component.energy.EnhancedEnergyLevelSelector
import com.zenithtasks.ui.component.task.TaskList
import com.zenithtasks.ui.viewmodel.EnergyMatchingViewModel

@Composable
fun EnergyMatchingScreen(
    viewModel: EnergyMatchingViewModel,
    onNavigateToTaskDetail: (Long) -> Unit,
    onNavigateToTaskList: () -> Unit,
    onNavigateToFocus: () -> Unit
) {
    val userEnergyLevel by viewModel.userEnergyLevel.collectAsState()
    val tasks by viewModel.energyFilteredTasks.collectAsState()
    val suggestedTasks by viewModel.suggestedTasks.collectAsState()
    val currentSortOrder by viewModel.sortOrder.collectAsState()
    
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            AdaptiveHeader(
                title = "Energy Matching",
                actions = {
                    SortMenu(
                        currentSort = currentSortOrder,
                        onSortChanged = viewModel::setSortOrder
                    )
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true }
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier.fillMaxSize()
            ) {
                Text(
                    text = "How's your energy today?",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                EnhancedEnergyLevelSelector(
                    selectedLevel = userEnergyLevel,
                    onEnergyLevelSelected = { viewModel.setUserEnergyLevel(it) }
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (suggestedTasks.isNotEmpty()) {
                    Text(
                        text = "Suggested for your energy level",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(16.dp)
                    )

                    TaskList(
                        tasks = suggestedTasks,
                        onTaskCompleted = { viewModel.completeTask(it) },
                        onTaskDeleted = { viewModel.deleteTask(it) },
                        onTaskArchived = { viewModel.archiveTask(it) },
                        onTaskClick = { task -> onNavigateToTaskDetail(task.id) },
                        onTaskPriorityChanged = { task, priority -> viewModel.updateTaskPriority(task, priority) },
                        onTaskSnooze = { /* Handle snooze */ },
                        onTaskUnsnooze = { /* Handle unsnooze */ }
                    )
                }

                Text(
                    text = "All matching tasks",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(16.dp)
                )

                TaskList(
                    tasks = tasks,
                    onTaskCompleted = { viewModel.completeTask(it) },
                    onTaskDeleted = { viewModel.deleteTask(it) },
                    onTaskArchived = { viewModel.archiveTask(it) },
                    onTaskClick = { task -> onNavigateToTaskDetail(task.id) },
                    onTaskSnooze = { /* Handle snooze */ },
                    onTaskUnsnooze = { /* Handle unsnooze */ }
                )
            }
        }
    }
    
    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onTaskCreated = { task ->
                viewModel.addTask(task)
                showAddTaskDialog = false
            },
            energyLevel = userEnergyLevel
        )
    }
}

