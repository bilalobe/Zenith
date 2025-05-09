package com.zenithtasks.widget

import android.content.Context
import android.util.Log
import androidx.glance.GlanceId
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.state.updateAppWidgetState
import com.zenithtasks.di.RepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/**
 * Utility class to manage focus widget updates
 */
class FocusWidgetManager(private val context: Context) {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val TAG = "FocusWidgetManager"
    
    /**
     * Update all instances of the focus widget
     */
    fun updateAllWidgets() {
        scope.launch {
            try {
                val glanceAppWidgetManager = GlanceAppWidgetManager(context)
                val glanceIds = glanceAppWidgetManager.getGlanceIds(FocusWidget::class.java)
                
                // Get repositories via entry point
                val entryPoint = EntryPointAccessors.fromApplication(
                    context.applicationContext,
                    RepositoryEntryPoint::class.java
                )
                val focusSessionRepository = entryPoint.focusSessionRepository()
                
                // Get current focus session state
                val activeSession = focusSessionRepository.getActiveFocusSession().first()
                val isFocusActive = activeSession != null
                
                // Update all widget instances
                glanceIds.forEach { glanceId ->
                    updateWidget(glanceId, isFocusActive)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error updating widgets", e)
            }
        }
    }
    
    /**
     * Update a specific widget instance
     */
    private suspend fun updateWidget(glanceId: GlanceId, isFocusActive: Boolean) {
        try {
            // Update the widget state
            updateAppWidgetState(context, glanceId) { prefs ->
                prefs[FocusWidget.IS_FOCUS_ACTIVE] = isFocusActive
            }
            
            // Request a refresh of the widget UI
            FocusWidget().update(context, glanceId)
        } catch (e: Exception) {
            Log.e(TAG, "Error updating widget $glanceId", e)
        }
    }
    
    companion object {
        /**
         * Static method to update all widgets from anywhere in the app
         */
        fun updateWidgets(context: Context) {
            FocusWidgetManager(context).updateAllWidgets()
        }
    }
}