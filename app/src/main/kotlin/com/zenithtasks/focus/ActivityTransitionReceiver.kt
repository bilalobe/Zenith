package com.zenithtasks.focus

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.content.edit
import com.google.android.gms.location.ActivityRecognitionResult
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionEvent
import com.google.android.gms.location.ActivityTransitionResult
import com.google.android.gms.location.DetectedActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

/**
 * BroadcastReceiver for handling activity transition events from the Activity Recognition API
 */
class ActivityTransitionReceiver : BroadcastReceiver() {

    private val transitionReceiverLogTag = "ActivityTransitionReceiver"
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)

    override fun onReceive(context: Context, intent: Intent) {
        if (ActivityTransitionResult.hasResult(intent)) {
            val result = ActivityTransitionResult.extractResult(intent)
            result?.let { handleActivityTransitionResult(context, it) }
        } else if (ActivityRecognitionResult.hasResult(intent)) {
            val result = ActivityRecognitionResult.extractResult(intent)
            result?.let { handleActivityRecognitionResult(context, it) }
        }
    }

    private fun handleActivityTransitionResult(context: Context, result: ActivityTransitionResult) {
        for (event in result.transitionEvents) {
            // Log the activity transition
            val activityType = getActivityString(event.activityType)
            val transitionType = getTransitionTypeString(event.transitionType)
            Log.d(transitionReceiverLogTag, "Activity Transition: $activityType $transitionType")

            // Update focus state with the detected activity
            updateFocusWithActivity(context, event)
        }
    }

    private fun handleActivityRecognitionResult(context: Context, result: ActivityRecognitionResult) {
        val mostProbableActivity = result.mostProbableActivity
        val activityType = getActivityString(mostProbableActivity.type)
        val confidence = mostProbableActivity.confidence
        
        Log.d(transitionReceiverLogTag, "Detected Activity: $activityType with confidence $confidence")
        
        // Only process high-confidence activities
        if (confidence >= 75) {
            updateFocusWithMostProbableActivity(context, mostProbableActivity)
        }
    }

    private fun updateFocusWithActivity(context: Context, event: ActivityTransitionEvent) {
        scope.launch {
            try {
                // Get the FocusStateManager and update the current activity
                val focusPrefs = context.getSharedPreferences(FOCUS_PREFS, Context.MODE_PRIVATE)
                val currentlyInFocus = focusPrefs.getBoolean(KEY_FOCUS_MODE_ACTIVE, false)
                
                if (currentlyInFocus) {
                    val activityType = when (event.activityType) {
                        DetectedActivity.STILL -> FocusActivityType.STILL
                        DetectedActivity.IN_VEHICLE -> FocusActivityType.DRIVING
                        DetectedActivity.WALKING -> FocusActivityType.WALKING
                        else -> FocusActivityType.UNKNOWN
                    }
                    
                    // Store the current activity
                    focusPrefs.edit {
                        putString(KEY_CURRENT_ACTIVITY, activityType.name)
                        }
                    
                    // Broadcast the activity change for the FocusViewModel to handle
                    val activityIntent = Intent(ACTION_ACTIVITY_CHANGED)
                    activityIntent.putExtra(EXTRA_ACTIVITY_TYPE, activityType.name)
                    context.sendBroadcast(activityIntent)
                }
            } catch (e: Exception) {
                Log.e(transitionReceiverLogTag, "Error updating focus with activity", e)
            }
        }
    }

    private fun updateFocusWithMostProbableActivity(context: Context, activity: DetectedActivity) {
        // Similar to updateFocusWithActivity but for most probable activity
        scope.launch {
            try {
                val focusPrefs = context.getSharedPreferences(FOCUS_PREFS, Context.MODE_PRIVATE)
                val currentlyInFocus = focusPrefs.getBoolean(KEY_FOCUS_MODE_ACTIVE, false)
                
                if (currentlyInFocus) {
                    val activityType = when (activity.type) {
                        DetectedActivity.STILL -> FocusActivityType.STILL
                        DetectedActivity.IN_VEHICLE -> FocusActivityType.DRIVING
                        DetectedActivity.WALKING -> FocusActivityType.WALKING
                        else -> FocusActivityType.UNKNOWN
                    }
                    
                    // Store the current activity
                    focusPrefs.edit {
                        putString(KEY_CURRENT_ACTIVITY, activityType.name)
                    }
                    
                    // Broadcast the activity change
                    val activityIntent = Intent(ACTION_ACTIVITY_CHANGED)
                    activityIntent.putExtra(EXTRA_ACTIVITY_TYPE, activityType.name)
                    context.sendBroadcast(activityIntent)
                }
            } catch (e: Exception) {
                Log.e(transitionReceiverLogTag, "Error updating focus with most probable activity", e)
            }
        }
    }

    private fun getActivityString(activityType: Int): String {
        return when (activityType) {
            DetectedActivity.STILL -> "STILL"
            DetectedActivity.WALKING -> "WALKING"
            DetectedActivity.RUNNING -> "RUNNING"
            DetectedActivity.IN_VEHICLE -> "IN_VEHICLE"
            DetectedActivity.ON_BICYCLE -> "ON_BICYCLE"
            DetectedActivity.ON_FOOT -> "ON_FOOT"
            DetectedActivity.TILTING -> "TILTING"
            DetectedActivity.UNKNOWN -> "UNKNOWN"
            else -> "UNKNOWN"
        }
    }

    private fun getTransitionTypeString(transitionType: Int): String {
        return when (transitionType) {
            ActivityTransition.ACTIVITY_TRANSITION_ENTER -> "ENTER"
            ActivityTransition.ACTIVITY_TRANSITION_EXIT -> "EXIT"
            else -> "UNKNOWN"
        }
    }

    companion object {
        const val FOCUS_PREFS = "focus_preferences"
        const val KEY_FOCUS_MODE_ACTIVE = "focus_mode_active"
        const val KEY_CURRENT_ACTIVITY = "current_activity"
        const val ACTION_ACTIVITY_CHANGED = "com.zenithtasks.ACTION_ACTIVITY_CHANGED"
        const val EXTRA_ACTIVITY_TYPE = "activity_type"
    }
}