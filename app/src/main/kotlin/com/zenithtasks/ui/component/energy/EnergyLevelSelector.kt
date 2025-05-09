package com.zenithtasks.ui.component.energy

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.local.entity.EnergyLevel

@Composable
fun EnergyLevelSelector(
    selectedLevel: EnergyLevel,
    onEnergyLevelSelected: (EnergyLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .selectableGroup()
            .fillMaxWidth()
    ) {
        EnergyLevel.entries.forEach { level ->
            val energyColor = when (level) {
                EnergyLevel.LOW -> Color(0xFF8BC34A)
                EnergyLevel.MEDIUM -> Color(0xFFFFA000)
                EnergyLevel.HIGH -> Color(0xFFF44336)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .selectable(
                        selected = (level == selectedLevel),
                        onClick = { onEnergyLevelSelected(level) },
                        role = Role.RadioButton
                    ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (level == selectedLevel) 8.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (level == selectedLevel)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                // Card content...
            }
            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

@Composable
fun EnhancedEnergyLevelSelector(
    selectedLevel: EnergyLevel,
    onEnergyLevelSelected: (EnergyLevel) -> Unit
) {
    Column(
        modifier = Modifier
            .selectableGroup()
            .fillMaxWidth()
    ) {
        EnergyLevel.entries.forEach { level ->
            val energyColor = when (level) {
                EnergyLevel.LOW -> Color(0xFF8BC34A)
                EnergyLevel.MEDIUM -> Color(0xFFFFA000)
                EnergyLevel.HIGH -> Color(0xFFF44336)
            }

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .selectable(
                        selected = (level == selectedLevel),
                        onClick = { onEnergyLevelSelected(level) },
                        role = Role.RadioButton
                    ),
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(
                    defaultElevation = if (level == selectedLevel) 8.dp else 2.dp
                ),
                colors = CardDefaults.cardColors(
                    containerColor = if (level == selectedLevel)
                        MaterialTheme.colorScheme.primaryContainer
                    else
                        MaterialTheme.colorScheme.surface
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(energyColor)
                    )

                    Spacer(modifier = Modifier.padding(horizontal = 8.dp))

                    Column(
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = when (level) {
                                EnergyLevel.LOW -> "Low Energy"
                                EnergyLevel.MEDIUM -> "Medium Energy"
                                EnergyLevel.HIGH -> "High Energy"
                            },
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )

                        Text(
                            text = when (level) {
                                EnergyLevel.LOW -> "Just basics today, please!"
                                EnergyLevel.MEDIUM -> "I can handle regular tasks"
                                EnergyLevel.HIGH -> "I can tackle anything!"
                            },
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }

                    RadioButton(
                        selected = (level == selectedLevel),
                        onClick = null
                    )
                }
            }

            Spacer(modifier = Modifier.height(4.dp))
        }
    }
}

