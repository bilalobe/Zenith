package com.zenithtasks.ui.component.analytics

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.zenithtasks.ui.viewmodel.TaskAnalyticsViewModel

/**
 * A card component showing the distribution of tasks by energy level.
 */
@Composable
fun EnergyDistributionCard(stats: TaskAnalyticsViewModel.TaskStats) {
    Card {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                text = "Task Distribution by Energy Level",
                style = MaterialTheme.typography.titleLarge
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // High energy
            EnergyRow(
                label = "High Energy",
                count = stats.highEnergyCount,
                color = Color(0xFF1976D2),
                icon = Icons.Default.BatteryFull
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Medium energy
            EnergyRow(
                label = "Medium Energy",
                count = stats.mediumEnergyCount,
                color = Color(0xFFFFC107),
                icon = Icons.Default.Bolt
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // Low energy
            EnergyRow(
                label = "Low Energy",
                count = stats.lowEnergyCount,
                color = Color(0xFF4CAF50),
                icon = Icons.Default.WaterDrop
            )
        }
    }
}

@Composable
private fun EnergyRow(
    label: String,
    count: Int,
    color: Color,
    icon: ImageVector
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(16.dp)
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