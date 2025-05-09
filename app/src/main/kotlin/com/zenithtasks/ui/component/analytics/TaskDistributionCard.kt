package com.zenithtasks.ui.component.analytics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zenithtasks.ui.viewmodel.TaskAnalyticsViewModel

/**
 * A card component showing the distribution of tasks by priority.
 */
@Composable
fun TaskDistributionCard(stats: TaskAnalyticsViewModel.TaskStats) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Task Distribution by Priority",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // High priority
            PriorityRow(
                label = "High Priority",
                count = stats.highPriorityCount,
                color = Color(0xFFF44336)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Medium priority
            PriorityRow(
                label = "Medium Priority",
                count = stats.mediumPriorityCount,
                color = Color(0xFFFF9800)
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Low priority
            PriorityRow(
                label = "Low Priority",
                count = stats.lowPriorityCount,
                color = Color(0xFF4CAF50)
            )
        }
    }
}

@Composable
private fun PriorityRow(
    label: String,
    count: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        // Priority color indicator
        Spacer(
            modifier = Modifier
                .size(12.dp)
                .clip(CircleShape)
                .background(color)
        )
        
        Spacer(modifier = Modifier.width(8.dp))
        
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )
        
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
    }
}