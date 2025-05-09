package com.zenithtasks.ui.component.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.local.entity.EnergyLevel

@Composable
fun EnergyChoice(
    energyLevel: EnergyLevel,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val icon = when (energyLevel) {
        EnergyLevel.LOW -> Icons.Default.WaterDrop
        EnergyLevel.MEDIUM -> Icons.Default.Bolt
        EnergyLevel.HIGH -> Icons.Default.BatteryFull
    }
    
    val color = when (energyLevel) {
        EnergyLevel.LOW -> Color(0xFF4CAF50)
        EnergyLevel.MEDIUM -> Color(0xFFFFC107)
        EnergyLevel.HIGH -> Color(0xFF1976D2)
    }
    
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent)
            .padding(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Text(
            text = energyLevel.name.lowercase().replaceFirstChar { it.uppercase() },
            style = MaterialTheme.typography.bodySmall
        )
    }
}