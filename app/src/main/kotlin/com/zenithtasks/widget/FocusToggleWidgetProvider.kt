package com.zenithtasks.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.RemoteViews
import android.widget.Toast
import com.zenithtasks.MainActivity
import com.zenithtasks.R
import com.zenithtasks.focus.FocusControlService
import com.zenithtasks.focus.FocusState
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.concurrent.TimeUnit
import javax.inject.Inject

/**
 * Implementation of App Widget functionality for the Focus Toggle widget.
 * Allows quick toggling of focus mode from the home screen.
 * 
 * Features:
 * - Toggle focus mode on/off with a single tap
 * - Shows current focus state with visual indicators
 * - Shows remaining focus time when focus mode is active
 * - Long press opens duration selection
 */
@AndroidEntryPoint
class FocusToggleWidgetProvider : AppWidgetProvider() {

    @Inject
    lateinit var focusRepository: com.zenithtasks.focus.FocusRepository
    
    // Create a scoped coroutine context with error handling
    private val widgetCoroutineScope = CoroutineScope(
        SupervisorJob() + 
        Dispatchers.IO + 
        CoroutineExceptionHandler { _, throwable ->
            Log.e(TAG, "Error in widget coroutine", throwable)
        }
    )

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        widgetCoroutineScope.launch {
            try {
                val focusState = getFocusState() ?: FocusState.Inactive
                
                // There may be multiple widgets active, so update all of them
                for (appWidgetId in appWidgetIds) {
                    updateAppWidget(context, appWidgetManager, appWidgetId, focusState)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widget", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, 
                        "Failed to update Focus widget: ${e.message}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    // Helper function to get focus state safely
    private suspend fun getFocusState(): FocusState? {
        return try {
            focusRepository.focusState
                .catch { error ->
                    Log.e(TAG, "Error fetching focus state", error)
                    emit(FocusState.Inactive)
                }
                .firstOrNull()
        } catch (e: Exception) {
            Log.e(TAG, "Exception getting focus state", e)
            FocusState.Inactive
        }
    }

    override fun onAppWidgetOptionsChanged(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        newOptions: Bundle?
    ) {
        super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions)
        // Refresh widget when its size changes
        refreshWidgets(context)
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        
        when (intent.action) {
            ACTION_TOGGLE_FOCUS -> handleToggleFocus(context)
            ACTION_SELECT_DURATION -> launchDurationSelector(context)
            ACTION_REFRESH_WIDGETS -> refreshWidgets(context)
        }
    }
    
    private fun handleToggleFocus(context: Context) {
        widgetCoroutineScope.launch {
            try {
                val currentState = getFocusState() ?: FocusState.Inactive
                
                if (currentState is FocusState.Active) {
                    // Turn off focus mode
                    val serviceIntent = Intent(context, FocusControlService::class.java).apply {
                        action = FocusControlService.ACTION_STOP_FOCUS
                    }
                    context.startService(serviceIntent)
                    
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            context, 
                            "Focus mode deactivated", 
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    // Turn on focus mode with default duration (25 minutes for pomodoro)
                    startFocus(context, DEFAULT_FOCUS_DURATION)
                }
                
                // Refresh all widgets
                refreshWidgets(context)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling focus", e)
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        context, 
                        "Failed to toggle focus mode: ${e.message}", 
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }
    
    private fun launchDurationSelector(context: Context) {
        // Open the main activity with the duration selection screen
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
            action = ACTION_SELECT_DURATION
        }
        context.startActivity(intent)
    }

    companion object {
        private const val TAG = "FocusToggleWidget"
        private const val ACTION_TOGGLE_FOCUS = "com.zenithtasks.widget.ACTION_TOGGLE_FOCUS"
        private const val ACTION_SELECT_DURATION = "com.zenithtasks.widget.ACTION_SELECT_DURATION"
        private const val ACTION_REFRESH_WIDGETS = "com.zenithtasks.widget.ACTION_REFRESH_WIDGETS"
        
        // Default focus durations
        private const val DEFAULT_FOCUS_DURATION = 25L // Pomodoro default (25 minutes)
        private const val SHORT_FOCUS_DURATION = 5L // Short break (5 minutes)
        private const val LONG_FOCUS_DURATION = 60L // Long focus session (1 hour)
        
        /**
         * Start a focus session with the specified duration
         */
        fun startFocus(context: Context, durationMinutes: Long) {
            val serviceIntent = Intent(context, FocusControlService::class.java).apply {
                action = FocusControlService.ACTION_START_FOCUS
                putExtra(FocusControlService.EXTRA_DURATION_MINUTES, durationMinutes)
            }
            context.startService(serviceIntent)
            
            Toast.makeText(
                context,
                "Focus mode activated for $durationMinutes minutes",
                Toast.LENGTH_SHORT
            ).show()
            
            // Refresh all widgets
            refreshWidgets(context)
        }
        
        /**
         * Trigger an update for all instances of the Focus Toggle Widget
         */
        fun refreshWidgets(context: Context) {
            val appWidgetManager = AppWidgetManager.getInstance(context)
            val appWidgetIds = appWidgetManager.getAppWidgetIds(
                ComponentName(context, FocusToggleWidgetProvider::class.java)
            )
            
            // Use Intent to trigger onUpdate
            val updateIntent = Intent(context, FocusToggleWidgetProvider::class.java).apply {
                action = AppWidgetManager.ACTION_APPWIDGET_UPDATE
                putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds)
            }
            context.sendBroadcast(updateIntent)
        }
    }

    private fun updateAppWidget(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetId: Int,
        focusState: FocusState
    ) {
        // Construct the RemoteViews object
        val views = RemoteViews(context.packageName, R.layout.widget_focus_toggle)
        
        when (focusState) {
            is FocusState.Active -> {
                // Calculate remaining time
                val remainingMinutes = calculateRemainingTime(focusState)
                
                // Update UI for active state
                views.setInt(R.id.widget_focus_container, "setBackgroundResource", 
                    R.drawable.widget_focus_background_active)
                views.setImageViewResource(R.id.image_focus_icon, R.drawable.ic_focus_on)
                
                // Show remaining time
                views.setTextViewText(
                    R.id.text_focus_status, 
                    if (remainingMinutes > 0) {
                        context.getString(R.string.focus_time_remaining, remainingMinutes)
                    } else {
                        context.getString(R.string.focus_almost_done)
                    }
                )
                
                // Use DEFAULT_FOCUS_DURATION as fallback if property doesn't exist
                val totalDuration = try {
                    focusState.javaClass.getDeclaredField("durationMinutes").apply { isAccessible = true }
                        .get(focusState) as? Long ?: DEFAULT_FOCUS_DURATION
                } catch (e: Exception) {
                    DEFAULT_FOCUS_DURATION
                }
                
                val progress = if (totalDuration > 0) {
                    ((totalDuration - remainingMinutes) / totalDuration.toFloat() * 100).toInt()
                } else 0

                views.setProgressBar(R.id.progress_focus, 100, progress, false)
                views.setViewVisibility(R.id.progress_focus, android.view.View.VISIBLE)
            }
            else -> {
                // Update UI for inactive state
                views.setInt(R.id.widget_focus_container, "setBackgroundResource", 
                    R.drawable.widget_focus_background_inactive)
                views.setImageViewResource(R.id.image_focus_icon, R.drawable.ic_focus_off)
                views.setTextViewText(R.id.text_focus_status, context.getString(R.string.focus_off))
                views.setViewVisibility(R.id.progress_focus, android.view.View.GONE)
            }
        }
        
        // Create an Intent to toggle focus mode when widget is clicked
        val toggleIntent = Intent(context, FocusToggleWidgetProvider::class.java).apply {
            action = ACTION_TOGGLE_FOCUS
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, 0, toggleIntent, 
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        views.setOnClickPendingIntent(R.id.widget_focus_container, pendingIntent)
        
        // Create an Intent for long press to select duration
        val durationIntent = Intent(context, FocusToggleWidgetProvider::class.java).apply {
            action = ACTION_SELECT_DURATION
        }
        val durationPendingIntent = PendingIntent.getBroadcast(
            context, 1, durationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        // Add shortcuts for different durations (Android 7.1+)
        val shortcutIntents = arrayOf(
            createDurationShortcut(context, 25, "Pomodoro (25 min)"),
            createDurationShortcut(context, 60, "Long Session (60 min)"),
            createDurationShortcut(context, 5, "Short Break (5 min)")
        )
        views.setRemoteAdapter(R.id.widget_shortcuts_list,
            Intent(context, FocusShortcutsService::class.java))

        // Update the widget
        try {
            appWidgetManager.updateAppWidget(appWidgetId, views)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating app widget", e)
        }
    }
    
    /**
     * Create a shortcut intent for a specific focus duration
     */
    private fun createDurationShortcut(
        context: Context, 
        durationMinutes: Long,
        label: String
    ): Intent {
        return Intent(context, FocusToggleWidgetProvider::class.java).apply {
            action = FocusControlService.ACTION_START_FOCUS
            putExtra(FocusControlService.EXTRA_DURATION_MINUTES, durationMinutes)
            putExtra("shortcut_label", label)
        }
    }
    
    /**
     * Calculate remaining focus time in minutes
     */
    private fun calculateRemainingTime(focusState: FocusState.Active): Long {
        val currentTimeMillis = System.currentTimeMillis()
        // Using reflection to safely access properties that might not exist in the class
        val endTime = try {
            focusState.javaClass.getDeclaredField("endTime").apply { isAccessible = true }.get(focusState) as? Long
        } catch (e: Exception) {
            null
        }
        
        val durationMinutes = try {
            focusState.javaClass.getDeclaredField("durationMinutes").apply { isAccessible = true }.get(focusState) as? Long ?: 25L
        } catch (e: Exception) {
            25L // Default to 25 minutes if field doesn't exist
        }
        
        val endTimeMillis = endTime ?: (currentTimeMillis + TimeUnit.MINUTES.toMillis(durationMinutes))
        
        val remainingMillis = endTimeMillis - currentTimeMillis
        return if (remainingMillis > 0) {
            TimeUnit.MILLISECONDS.toMinutes(remainingMillis)
        } else {
            0
        }
    }
}