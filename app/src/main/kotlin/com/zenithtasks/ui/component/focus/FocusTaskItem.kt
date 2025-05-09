package com.zenithtasks.ui.component.focus

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.local.entity.EnergyLevel
import com.zenithtasks.data.model.Task
import java.text.SimpleDateFormat
import java.util.Locale

/**
 * A task item designed specifically for the focus mode screen.
 * It displays task information with expandable description.
 * 
 * @param task The task to display
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FocusTaskItem(task: Task) {
    var expanded by remember { mutableStateOf(false) }
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(12.dp),
        onClick = { expanded = !expanded }
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Energy level indicator
                val energyColor = when (task.energyLevel) {
                    EnergyLevel.LOW -> Color(0xFF8BC34A)
                    EnergyLevel.MEDIUM -> Color(0xFFFFA000)
                    EnergyLevel.HIGH -> Color(0xFFF44336)
                }
                
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(energyColor)
                )
                
                Spacer(modifier = Modifier.padding(horizontal = 8.dp))
                
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    maxLines = if (expanded) Int.MAX_VALUE else 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            
            if (expanded && task.description != null) {
                Spacer(modifier = Modifier.height(8.dp))
                
                Text(
                    text = task.description,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
            
            Spacer(modifier = Modifier.height(4.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End
            ) {
                Text(
                    text = when (task.energyLevel) {
                        EnergyLevel.LOW -> "Low Energy"
                        EnergyLevel.MEDIUM -> "Medium Energy"
                        EnergyLevel.HIGH -> "High Energy"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                task.dueDate?.let {
                    val formattedDate = SimpleDateFormat("MMM d", Locale.getDefault()).format(it)
                    Text(
                        text = " · Due: $formattedDate",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}