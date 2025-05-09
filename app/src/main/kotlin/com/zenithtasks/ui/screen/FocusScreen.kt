package com.zenithtasks.ui.screen

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.zenithtasks.focus.FocusState
import com.zenithtasks.ui.component.focus.ActivityCard
import com.zenithtasks.ui.component.focus.FocusModeToggleCard
import com.zenithtasks.ui.component.focus.TaskSuggestionsSection
import com.zenithtasks.ui.viewmodel.FocusViewModel
import java.time.Duration

/**
 * A screen that allows users to enter focus mode and see task suggestions based on their current activity.
 */
@Composable
fun FocusScreen(
    viewModel: FocusViewModel = hiltViewModel()
) {
    val focusState by viewModel.focusState.collectAsState()
    val systemDndActive by viewModel.systemDndActive.collectAsState()
    val suggestedTasks by viewModel.suggestedFocusTasks.collectAsState()
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Header section
            Text(
                text = "Focus Mode",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = "Minimize distractions and focus on tasks that matter",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            
            Spacer(modifier = Modifier.height(24.dp))
            
            // Focus mode toggle
            FocusModeToggleCard(
                focusState = focusState,
                systemDndActive = systemDndActive,
                onToggleFocus = { enabled ->
                    if (enabled) {
                        viewModel.startFocusMode(Duration.ofHours(1))
                    } else {
                        viewModel.stopFocusMode()
                    }
                },
                onOpenSystemSettings = {
                    viewModel.openSystemDndSettings()
                }
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // Current activity indicator (if in focus mode)
            if (focusState is FocusState.Active) {
                val activity = (focusState as FocusState.Active).activityType
                activity?.let {
                    ActivityCard(activityType = it)
                }
                
                Spacer(modifier = Modifier.height(16.dp))
            }
            
            // Task suggestions
            TaskSuggestionsSection(
                tasks = suggestedTasks,
                isInFocusMode = focusState is FocusState.Active
            )
        }
    }
}