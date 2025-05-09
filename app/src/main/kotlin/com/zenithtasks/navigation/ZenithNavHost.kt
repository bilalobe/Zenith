package com.zenithtasks.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.zenithtasks.ui.screen.FocusScreen

/**
 * Navigation routes for the app
 */
sealed class ZenithDestination(val route: String) {
    object TaskList : ZenithDestination("taskList")
    object TaskDetail : ZenithDestination("taskDetail/{taskId}") {
        fun createRoute(taskId: Long) = "taskDetail/$taskId"
    }
    object CreateTask : ZenithDestination("createTask")
    object EnergyMatching : ZenithDestination("energyMatching")
    object Focus : ZenithDestination("focus")
    object Settings : ZenithDestination("settings")
}

/**
 * Main navigation component for the Zenith app
 */
@Composable
fun ZenithNavHost(
    navController: NavHostController,
    startDestination: String = ZenithDestination.TaskList.route
) {
    NavHost(
        navController = navController,
        startDestination = startDestination
    ) {
        // Add all destinations here
        
        // Task List
        composable(route = ZenithDestination.TaskList.route) {
            // Replace with your actual task list screen
            // TaskListScreen(navController = navController)
        }
        
        // Task Detail
        composable(
            route = ZenithDestination.TaskDetail.route
        ) { backStackEntry ->
            val taskId = backStackEntry.arguments?.getString("taskId")?.toLongOrNull()
            if (taskId != null) {
                TaskDetailScreen(taskId = taskId, navController = navController)
            }
        }
        
        // Create Task
        composable(route = ZenithDestination.CreateTask.route) {
            // Replace with your actual create task screen
            // CreateTaskScreen(navController = navController)
        }
        
        // Energy Matching
        composable(route = ZenithDestination.EnergyMatching.route) {
            // Replace with your actual energy matching screen
            // EnergyMatchingScreen(navController = navController)
        }
        
        // Focus Mode - our new screen
        composable(route = ZenithDestination.Focus.route) {
            FocusScreen() // This is the screen we just created
        }
        
        // Settings
        composable(route = ZenithDestination.Settings.route) {
            // Replace with your actual settings screen
            // SettingsScreen(navController = navController)
        }
    }
}

@Composable
fun TaskDetailScreen(taskId: Long, navController: NavHostController) {
    TODO("Not yet implemented")
}