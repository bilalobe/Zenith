package com.zenithtasks.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun SwipeBackground(
    color: Color,
    icon: ImageVector,
    text: String,
    alignment: Alignment,
    progress: Float
) {
    val scale = 0.8f + (0.2f * minOf(1f, progress * 2.5f))
    val alpha = minOf(1f, progress * 2.5f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                color = color.copy(alpha = alpha * 0.8f),
                shape = RoundedCornerShape(12.dp)
            )
            .padding(horizontal = 20.dp),
        contentAlignment = alignment
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (alignment == Alignment.CenterStart)
                Arrangement.Start else Arrangement.End,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (alignment == Alignment.CenterStart) {
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier
                        .scale(scale)
                        .padding(end = 8.dp)
                )
                if (progress > 0.3f) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(alpha)
                    )
                }
            } else {
                if (progress > 0.3f) {
                    Text(
                        text = text,
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.alpha(alpha)
                    )
                }
                Icon(
                    imageVector = icon,
                    contentDescription = text,
                    tint = Color.White,
                    modifier = Modifier
                        .scale(scale)
                        .padding(start = 8.dp)
                )
            }
        }
    }
}
