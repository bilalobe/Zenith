package com.zenithtasks.widget

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.action.ActionParameters
import androidx.glance.appwidget.action.ActionCallback
import androidx.glance.appwidget.state.updateAppWidgetState
import com.zenithtasks.di.RepositoryEntryPoint
import dagger.hilt.android.EntryPointAccessors
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import java.util.Date

class FocusActionCallback : ActionCallback {
    private val IS_FOCUS_ACTIVE = booleanPreferencesKey("is_focus_active")
    
    override suspend fun onAction(
        context: Context,
        glanceId: GlanceId,
        parameters: ActionParameters
    ) {
        // Access Repositories via Hilt EntryPoint
        val hiltEntryPoint = EntryPointAccessors.fromApplication(
            context.applicationContext,
            RepositoryEntryPoint::class.java
        )
        val focusRepository = hiltEntryPoint.focusSessionRepository()
        val firebaseSyncRepo = hiltEntryPoint.firebaseSyncRepository()

        // Launch a coroutine to interact with the repository
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // Get the current active session (use first() to get the latest value)
                val activeSession = focusRepository.getActiveFocusSession().first()
                
                if (activeSession == null) {
                    // Start a new focus session
                    focusRepository.startFocusSession(Date())
                    
                    // Update widget state to reflect active focus
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[IS_FOCUS_ACTIVE] = true
                    }
                } else {
                    // End the current focus session
                    focusRepository.endFocusSession(activeSession, Date())
                    
                    // Update widget state to reflect inactive focus
                    updateAppWidgetState(context, glanceId) { prefs ->
                        prefs[IS_FOCUS_ACTIVE] = false
                    }
                }
                
                // Synchronize with Firebase if the user is signed in
                if (firebaseSyncRepo.isUserSignedIn()) {
                    firebaseSyncRepo.syncFocusSessions()
                }
                
                // Request widget update to reflect the state change
                val widget = FocusWidget()
                widget.update(context, glanceId)
                
            } catch (e: Exception) {
                // Log error but don't crash
                android.util.Log.e("FocusActionCallback", "Error toggling focus state", e)
                
                // Always update the widget even if there's an error
                try {
                    FocusWidget().update(context, glanceId)
                } catch (updateError: Exception) {
                    android.util.Log.e("FocusActionCallback", "Failed to update widget after error", updateError)
                }
            }
        }
    }
}