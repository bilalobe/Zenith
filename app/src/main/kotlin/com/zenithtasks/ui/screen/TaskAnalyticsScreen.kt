package com.zenithtasks.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenithtasks.ui.component.analytics.EnergyDistributionCard
import com.zenithtasks.ui.component.analytics.TaskDistributionCard
import com.zenithtasks.ui.component.common.AdaptiveHeader
import com.zenithtasks.ui.viewmodel.TaskAnalyticsViewModel

/**
 * A screen that displays analytics and statistics about tasks.
 */
@Composable
fun TaskAnalyticsScreen(
    viewModel: TaskAnalyticsViewModel = hiltViewModel()
) {
    val stats by viewModel.taskStats.collectAsState()
    
    Column(modifier = Modifier.padding(16.dp)) {
        AdaptiveHeader(title = "Task Analytics")
        
        Spacer(modifier = Modifier.height(16.dp))
        
        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "Task Completion Rate",
                    style = MaterialTheme.typography.titleLarge
                )
                
                Spacer(modifier = Modifier.height(8.dp))

                LinearProgressIndicator(
                progress = { stats.completionRate },
                modifier = Modifier.fillMaxWidth(),
                )
                
                Text(
                    text = "${(stats.completionRate * 100).toInt()}% completed",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
        
        // Placeholder for additional analytics cards
        Spacer(modifier = Modifier.height(16.dp))
        
        // Task distribution by priority
        TaskDistributionCard(stats = stats)
        
        // Task distribution by energy level
        Spacer(modifier = Modifier.height(16.dp))
        EnergyDistributionCard(stats = stats)
    }
}