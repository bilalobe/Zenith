package com.zenithtasks.ui.component.focus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.Task

/**
 * A section that displays task suggestions in focus mode.
 * 
 * @param tasks The list of tasks to display
 * @param isInFocusMode Whether focus mode is currently active
 */
@Composable
fun TaskSuggestionsSection(
    tasks: List<Task>,
    isInFocusMode: Boolean
) {
    Column {
        Text(
            text = if (isInFocusMode) "Suggested for current activity" else "Tasks you might focus on",
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.Bold
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (!isInFocusMode) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(bottom = 8.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Info,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
                
                Spacer(modifier = Modifier.padding(horizontal = 4.dp))
                
                Text(
                    text = "Enable focus mode to see personalized task suggestions",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        
        HorizontalDivider(
            modifier = Modifier.fillMaxWidth(),
            thickness = 1.dp,
            color = MaterialTheme.colorScheme.outlineVariant
        )
        
        Spacer(modifier = Modifier.height(8.dp))
        
        if (tasks.isEmpty()) {
            EmptyTaskSuggestions(isInFocusMode)
        } else {
            LazyColumn {
                items(tasks) { task ->
                    FocusTaskItem(task = task)
                }
            }
        }
    }
}