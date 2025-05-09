package com.zenithtasks.ui.screen

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.zenithtasks.ui.component.common.AdaptiveHeader
import com.zenithtasks.ui.component.task.detail.TaskDetailBody
import com.zenithtasks.ui.viewmodel.TaskDetailViewModel

/**
 * Screen for displaying task details.
 * 
 * @param taskId The ID of the task to display
 * @param viewModel The ViewModel for this screen
 * @param onBack Callback for navigating back
 */
@Composable
fun TaskDetailScreen(
    taskId: Long,
    viewModel: TaskDetailViewModel,
    onBack: () -> Unit
) {
    // State Hoisting: Retrieve state outside of the Scaffold to improve testability.
    val taskState by viewModel.task.collectAsState(initial = null)
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(taskId) {
        viewModel.loadTask(taskId)
    }

    Scaffold(
        topBar = {
            AdaptiveHeader(
                title = "Task Details",
                onBackClicked = onBack,
                actions = {
                    IconButton(onClick = { /* Handle edit mode here */ }) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Task"
                        )
                    }
                }
            )
        }
    ) { paddingValues ->
        TaskDetailBody(
            task = taskState,
            paddingValues = paddingValues,
            isLoading = uiState.isLoading
        )
    }
}