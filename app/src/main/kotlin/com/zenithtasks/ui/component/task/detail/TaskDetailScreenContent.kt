package com.zenithtasks.ui.component.task.detail

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import com.zenithtasks.data.model.Task
import com.zenithtasks.ui.component.common.AdaptiveHeader

@Composable
fun TaskDetailScreenContent(
    task: Task?,
    isLoading: Boolean,
    onBackClicked: () -> Unit,
    onEditClicked: () -> Unit
) {
    Scaffold(
        topBar = {
            AdaptiveHeader(
                title = "Task Details",
                onBackClicked = onBackClicked,
                actions = {
                    IconButton(onClick = onEditClicked) {
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
            task = task,
            isLoading = isLoading,
            paddingValues = paddingValues
        )
    }
}