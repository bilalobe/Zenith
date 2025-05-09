package com.zenithtasks.ui.component.common

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

/**
 * A flexible header component that adapts to different screen configurations,
 * including devices with notches and edge-to-edge displays.
 *
 * @param title The title text to display in the header
 * @param showGradient Whether to show a gradient background (default: true)
 * @param modifier Additional modifiers to apply
 * @param actions Optional composable for action buttons on the right side
 * @param backgroundColor Background color (ignored if gradient is enabled)
 * @param onAddClicked Optional callback for the "+" button click event
 * @param onBackClicked Optional callback for the back button click event
 */
@Composable
fun AdaptiveHeader(
    title: String,
    showGradient: Boolean = true,
    modifier: Modifier = Modifier,
    actions: @Composable (() -> Unit)? = null,
    backgroundColor: Color = MaterialTheme.colorScheme.primary,
    onAddClicked: (() -> Unit)? = null,
    onBackClicked: (() -> Unit)? = null
) {
    Surface(
        color = Color.Transparent,
        modifier = modifier.fillMaxWidth()
    ) {
        Column {
            // Define the background modifier conditionally for the status bar area
            val statusBarBackgroundModifier = if (showGradient) {
                Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.95f),
                            MaterialTheme.colorScheme.primary
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    ),
                    shape = RectangleShape
                )
            } else {
                Modifier.background(
                    color = backgroundColor,
                    shape = RectangleShape
                )
            }

            // Status bar padding - handles notches automatically through WindowInsets API
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .windowInsetsPadding(WindowInsets.statusBars)
                    .then(statusBarBackgroundModifier) // Apply the conditional modifier
            )

            // Define the background modifier conditionally for the header content area
            val headerContentBackgroundModifier = if (showGradient) {
                Modifier.background(
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primary,
                            MaterialTheme.colorScheme.primary.copy(alpha = 0.9f)
                        ),
                        startY = 0f,
                        endY = Float.POSITIVE_INFINITY
                    ),
                    shape = RectangleShape
                )
            } else {
                Modifier.background(
                    color = backgroundColor,
                    shape = RectangleShape
                )
            }

            // Header content with title and optional actions
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .then(headerContentBackgroundModifier) // Apply the conditional modifier
                    .padding(horizontal = 16.dp)
            ) {
                // Show back button if callback is provided
                if (onBackClicked != null) {
                    IconButton(
                        onClick = onBackClicked,
                        modifier = Modifier.align(Alignment.CenterStart)
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Navigate Back",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                    
                    // Center the title when back button is shown
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.align(Alignment.Center)
                    )
                } else {
                    // Align title to start when no back button
                    Text(
                        text = title,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.align(Alignment.CenterStart)
                    )
                }

                // Add button on the right
                if (onAddClicked != null) {
                    IconButton(
                        onClick = onAddClicked,
                        modifier = Modifier.align(Alignment.CenterEnd)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Add Task",
                            tint = MaterialTheme.colorScheme.onPrimary
                        )
                    }
                }

                // Other actions next to Add button if provided
                if (actions != null) {
                    Box(
                        modifier = Modifier
                            .align(Alignment.CenterEnd)
                            .padding(end = if (onAddClicked != null) 48.dp else 0.dp)
                    ) {
                        actions()
                    }
                }
            }
        }
    }
}