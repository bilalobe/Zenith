package com.zenithtasks.ui.component.focus

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoNotDisturb
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithtasks.focus.FocusState

/**
 * A card component that allows toggling focus mode and displays DND status.
 *
 * @param focusState The current focus state (active or inactive)
 * @param systemDndActive Whether system Do Not Disturb mode is active
 * @param onToggleFocus Callback when focus mode is toggled
 * @param onOpenSystemSettings Callback to open system DND settings
 */
@Composable
fun FocusModeToggleCard(
    focusState: FocusState,
    systemDndActive: Boolean,
    onToggleFocus: (Boolean) -> Unit,
    onOpenSystemSettings: () -> Unit
) {
    val isActive = focusState is FocusState.Active
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isActive) 
                MaterialTheme.colorScheme.primaryContainer 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Enable Focus Mode",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                
                Switch(
                    checked = isActive,
                    onCheckedChange = onToggleFocus
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Text(
                text = if (isActive) {
                    "Focus mode is active. Notifications are filtered, and suggested tasks are personalized."
                } else {
                    "Enable to minimize distractions and focus on tasks that match your current activity."
                },
                style = MaterialTheme.typography.bodyMedium
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DoNotDisturb,
                        contentDescription = "System DND",
                        tint = if (systemDndActive) 
                            MaterialTheme.colorScheme.primary 
                        else 
                            MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                    )
                    
                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                    
                    Text(
                        text = if (systemDndActive) "System DND active" else "System DND inactive",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                IconButton(onClick = onOpenSystemSettings) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = "Open System Settings"
                    )
                }
            }
        }
    }
}