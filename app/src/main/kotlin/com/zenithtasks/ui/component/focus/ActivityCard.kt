package com.zenithtasks.ui.component.focus

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.DirectionsWalk
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.TimerOff
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithtasks.focus.FocusActivityType

/**
 * A card component displaying the user's current activity type during focus mode.
 *
 * @param activityType The current activity type (STILL, WALKING, DRIVING, etc)
 */
@Composable
fun ActivityCard(activityType: FocusActivityType) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.secondaryContainer
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Activity icon
            val icon = when (activityType) {
                FocusActivityType.STILL -> Icons.Default.Timer
                FocusActivityType.DRIVING -> Icons.Default.DirectionsCar
                FocusActivityType.WALKING -> Icons.AutoMirrored.Filled.DirectionsWalk
                FocusActivityType.UNKNOWN -> Icons.Default.TimerOff
            }
            
            Icon(
                imageVector = icon,
                contentDescription = "Activity Type",
                modifier = Modifier.size(36.dp),
                tint = MaterialTheme.colorScheme.onSecondaryContainer
            )
            
            Spacer(modifier = Modifier.padding(horizontal = 16.dp))
            
            Column {
                Text(
                    text = "Current Activity",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
                
                Text(
                    text = when (activityType) {
                        FocusActivityType.STILL -> "Still (Deep Work)"
                        FocusActivityType.DRIVING -> "Driving"
                        FocusActivityType.WALKING -> "Walking"
                        FocusActivityType.UNKNOWN -> "Unknown"
                    },
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                    fontWeight = FontWeight.Bold
                )
                
                Text(
                    text = when (activityType) {
                        FocusActivityType.STILL -> "Showing tasks suitable for focused work"
                        FocusActivityType.DRIVING -> "Showing audio tasks for safe driving"
                        FocusActivityType.WALKING -> "Showing tasks suitable while on the move"
                        FocusActivityType.UNKNOWN -> "Showing general recommended tasks"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSecondaryContainer.copy(alpha = 0.7f)
                )
            }
        }
    }
}