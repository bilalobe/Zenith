package com.zenithtasks.ui.navigation

/**
 * Defines all navigation destinations in the app.
 */
sealed class Screen(val route: String) {
    object EnergyMatching : Screen("energy_matching")
    object TaskList : Screen("task_list")
    object TaskDetail : Screen("task_detail")
    object AddTask : Screen("add_task")
    object Focus : Screen("focus")
    object Account : Screen("account")
}