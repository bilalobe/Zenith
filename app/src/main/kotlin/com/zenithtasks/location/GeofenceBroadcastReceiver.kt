package com.zenithtasks.location

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofenceStatusCodes
import com.google.android.gms.location.GeofencingEvent
import com.zenithtasks.data.database.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * Broadcast receiver for handling geofence transition events.
 * This class is triggered when the user enters or exits a geofence.
 */
class GeofenceBroadcastReceiver : BroadcastReceiver() {

    private val TAG = "GeofenceBroadcastReceiver"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        val geofencingEvent = GeofencingEvent.fromIntent(intent)
        
        if (geofencingEvent == null) {
            Log.e(TAG, "GeofencingEvent is null")
            return
        }
        
        if (geofencingEvent.hasError()) {
            val errorMessage = GeofenceStatusCodes.getStatusCodeString(geofencingEvent.errorCode)
            Log.e(TAG, "Geofencing error: $errorMessage")
            return
        }

        // Get the transition type
        val geofenceTransition = geofencingEvent.geofenceTransition

        // Check if the transition type is of interest
        if (geofenceTransition == Geofence.GEOFENCE_TRANSITION_ENTER) {
            // Get the geofences that were triggered
            val triggeringGeofences = geofencingEvent.triggeringGeofences
            
            if (triggeringGeofences.isNullOrEmpty()) {
                Log.e(TAG, "No triggering geofences found")
                return
            }

            // Process each geofence
            for (geofence in triggeringGeofences) {
                val locationId = geofence.requestId.toLongOrNull()
                if (locationId != null) {
                    processGeofenceEnter(context, locationId)
                } else {
                    Log.e(TAG, "Invalid geofence ID: ${geofence.requestId}")
                }
            }
        }
    }

    /**
     * Process a geofence enter event.
     * This method retrieves tasks associated with the location and shows notifications.
     *
     * @param context The context
     * @param locationId The ID of the location that was entered
     */
    private fun processGeofenceEnter(context: Context, locationId: Long) {
        Log.d(TAG, "Processing geofence enter for location ID: $locationId")
        
        scope.launch {
            try {
                val database = AppDatabase.getInstance(context)
                
                // Get the location
                val location = database.locationDao().getLocationById(locationId)
                if (location == null) {
                    Log.e(TAG, "Location not found for ID: $locationId")
                    return@launch
                }
                
                Log.d(TAG, "Entered location: ${location.name}")
                
                // Get tasks for this location
                val tasks = database.taskDao().getTasksByLocationFlow(locationId)
                
                // Collect tasks from the flow and process them
                tasks.collect { taskList ->
                    for (task in taskList) {
                        if (!task.reminderTriggered && task.locationReminderEnabled && !task.isCompleted) {
                            // Mark reminder as triggered
                            database.taskDao().markReminderAsTriggered(task.id)
                            
                            // Show notification
                            val notificationHelper = NotificationHelper(context)
                            notificationHelper.showTaskNotification(task.id, task.title, location.name)
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error processing geofence enter", e)
            }
        }
    }
}