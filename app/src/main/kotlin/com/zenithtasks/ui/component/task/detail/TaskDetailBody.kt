package com.zenithtasks.ui.component.task.detail

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.zenithtasks.data.model.Task

@Composable
fun TaskDetailBody(
    task: Task?,
    isLoading: Boolean,
    paddingValues: PaddingValues
) {
    Box(
        modifier = Modifier
            .padding(paddingValues)
            .fillMaxSize()
    ) {
        if (isLoading) {
            CircularProgressIndicator(modifier = Modifier.align(Alignment.Center))
        } else {
            if (task != null) {
                TaskDetailContent(task = task)
            } else {
                Text(
                    text = "Task not found",
                    modifier = Modifier.align(Alignment.Center)
                )
            }
        }
    }
}