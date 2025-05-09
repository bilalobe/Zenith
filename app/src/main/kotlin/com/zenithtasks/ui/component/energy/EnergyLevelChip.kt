package com.zenithtasks.ui.component.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.local.entity.EnergyLevel

@Composable
fun EnergyLevelChip(energyLevel: EnergyLevel) {
    Box(
        modifier = Modifier
            .background(
                color = when (energyLevel) {
                    EnergyLevel.HIGH -> Color(0xFFE3F2FD).copy(alpha = 0.7f)
                    EnergyLevel.MEDIUM -> Color(0xFFFFF9C4).copy(alpha = 0.7f)
                    EnergyLevel.LOW -> Color(0xFFC6E1C7).copy(alpha = 0.7f)
                },
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = when (energyLevel) {
                    EnergyLevel.HIGH -> Icons.Default.BatteryFull
                    EnergyLevel.MEDIUM -> Icons.Default.Bolt
                    EnergyLevel.LOW -> Icons.Default.WaterDrop
                },
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = when (energyLevel) {
                    EnergyLevel.HIGH -> Color(0xFF1976D2)
                    EnergyLevel.MEDIUM -> Color(0xFFFFC107)
                    EnergyLevel.LOW -> Color(0xFF4CAF50)
                }
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = energyLevel.name.lowercase().replaceFirstChar { it.uppercase() },
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Medium,
                color = when (energyLevel) {
                    EnergyLevel.HIGH -> Color(0xFF1976D2)
                    EnergyLevel.MEDIUM -> Color(0xFFFFC107)
                    EnergyLevel.LOW -> Color(0xFF4CAF50)
                }
            )
        }
    }
}
