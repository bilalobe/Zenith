package com.zenithtasks.ui.component.task

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Archive
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Done
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import com.zenithtasks.data.model.Task
import com.zenithtasks.ui.component.common.SwipeAction
import com.zenithtasks.ui.component.common.SwipeBackground
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SwipeableTaskItem(
    task: Task,
    onTaskCompleted: () -> Unit,
    onTaskDeleted: () -> Unit,
    onTaskArchived: () -> Unit,
    onTaskClick: (Task) -> Unit = {}
) {
    var isTaskVisible by remember { mutableStateOf(true) }
    var swipeAction by remember { mutableStateOf(SwipeAction.NONE) }
    var taskToRestore by remember { mutableStateOf<Task?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val haptic = LocalHapticFeedback.current

    val floatAnimationSpec = remember {
        tween<Float>(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    }

    val intSizeAnimationSpec = remember {
        tween<IntSize>(
            durationMillis = 500,
            easing = FastOutSlowInEasing
        )
    }

    val archiveThreshold = 0.5f
    val deleteThreshold = 0.9f

    val dismissState = rememberSwipeToDismissBoxState(
        initialValue = SwipeToDismissBoxValue.Settled,
        positionalThreshold = { totalDistance -> totalDistance * 0.5f },
        confirmValueChange = { dismissValue ->
            when (dismissValue) {
                SwipeToDismissBoxValue.EndToStart -> {
                    when (swipeAction) {
                        SwipeAction.DELETE -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isTaskVisible = false
                            taskToRestore = task
                            scope.launch {
                                onTaskDeleted()
                                val result = snackbarHostState.showSnackbar(
                                    message = "'${task.title}' deleted",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    isTaskVisible = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        SwipeAction.ARCHIVE -> {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            isTaskVisible = false
                            taskToRestore = task
                            scope.launch {
                                onTaskArchived()
                                val result = snackbarHostState.showSnackbar(
                                    message = "'${task.title}' archived",
                                    actionLabel = "UNDO",
                                    duration = SnackbarDuration.Long
                                )
                                if (result == SnackbarResult.ActionPerformed) {
                                    isTaskVisible = true
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                }
                            }
                        }
                        else -> { /* Do nothing */ }
                    }
                    swipeAction = SwipeAction.NONE
                    true
                }
                SwipeToDismissBoxValue.StartToEnd -> {
                    swipeAction = SwipeAction.COMPLETE
                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                    isTaskVisible = false
                    taskToRestore = task
                    true
                }
                SwipeToDismissBoxValue.Settled -> {
                    swipeAction = SwipeAction.NONE
                    false
                }
            }
        }
    )

    LaunchedEffect(dismissState.targetValue, dismissState.progress) {
        if (dismissState.targetValue == SwipeToDismissBoxValue.EndToStart) {
            val progress = dismissState.progress
            swipeAction = when {
                progress > deleteThreshold -> SwipeAction.DELETE
                progress > archiveThreshold -> SwipeAction.ARCHIVE
                else -> SwipeAction.NONE
            }
            
            when (swipeAction) {
                SwipeAction.DELETE -> haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                SwipeAction.ARCHIVE -> haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                else -> {}
            }
        }
    }

    LaunchedEffect(dismissState.currentValue) {
        if (dismissState.currentValue == SwipeToDismissBoxValue.StartToEnd && taskToRestore != null) {
            onTaskCompleted()
            val currentTask = taskToRestore
            taskToRestore = null

            val result = snackbarHostState.showSnackbar(
                message = "'${currentTask?.title}' marked as completed",
                actionLabel = "UNDO",
                duration = SnackbarDuration.Short
            )

            if (result == SnackbarResult.ActionPerformed) {
                isTaskVisible = true
                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                scope.launch { dismissState.reset() }
            }
        }
    }

    Box {
        AnimatedVisibility(
            visible = isTaskVisible,
            exit = fadeOut(animationSpec = floatAnimationSpec) +
                  shrinkHorizontally(animationSpec = intSizeAnimationSpec)
        ) {
            SwipeToDismissBox(
                state = dismissState,
                enableDismissFromEndToStart = true,
                enableDismissFromStartToEnd = true,
                backgroundContent = {
                    val direction = dismissState.targetValue
                    val progress = dismissState.progress

                    when (direction) {
                        SwipeToDismissBoxValue.StartToEnd -> {
                            val color = Color(0xFF4CAF50)
                            val icon = Icons.Default.Done
                            val text = "Complete"
                            val alignment = Alignment.CenterStart

                            SwipeBackground(
                                color = color,
                                icon = icon,
                                text = text,
                                alignment = alignment,
                                progress = progress
                            )
                        }
                        SwipeToDismissBoxValue.EndToStart -> {
                            when (swipeAction) {
                                SwipeAction.DELETE -> {
                                    val color = Color(0xFFF44336)
                                    val icon = Icons.Default.Delete
                                    val text = "Delete"
                                    val alignment = Alignment.CenterEnd

                                    SwipeBackground(
                                        color = color,
                                        icon = icon, 
                                        text = text,
                                        alignment = alignment,
                                        progress = progress
                                    )
                                }
                                SwipeAction.ARCHIVE -> {
                                    val color = Color(0xFF607D8B)
                                    val icon = Icons.Default.Archive
                                    val text = "Archive"
                                    val alignment = Alignment.CenterEnd

                                    SwipeBackground(
                                        color = color,
                                        icon = icon, 
                                        text = text,
                                        alignment = alignment,
                                        progress = progress
                                    )
                                }
                                else -> {
                                    Box(modifier = Modifier.fillMaxSize())
                                }
                            }
                        }
                        else -> {
                            Box(modifier = Modifier.fillMaxSize())
                        }
                    }
                },
                content = {
                    TaskItem(
                        task = task,
                        onTaskClick = { onTaskClick(task) }
                    )
                }
            )
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 8.dp)
        )
    }
}



