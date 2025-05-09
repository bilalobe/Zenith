package com.zenithtasks.ui.component.focus

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

/**
 * A component to display when there are no task suggestions available.
 * 
 * @param isInFocusMode Whether focus mode is currently active
 */
@Composable
fun EmptyTaskSuggestions(isInFocusMode: Boolean) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 32.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (isInFocusMode) {
                "No tasks found for your current activity"
            } else {
                "Enable focus mode to get personalized task suggestions"
            },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}