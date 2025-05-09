package com.zenithtasks.focus

import android.Manifest
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.ActivityRecognition
import com.google.android.gms.location.ActivityRecognitionClient
import com.google.android.gms.location.ActivityTransition
import com.google.android.gms.location.ActivityTransitionRequest
import com.google.android.gms.location.DetectedActivity

/**
 * Manager for activity recognition to detect user's current activity
 * (still, walking, driving) for focus mode suggestions
 */
class ActivityRecognitionManager(private val context: Context) {

    private val activityRecognitionTag = "ActivityRecognitionMgr"
    private var activityRecognitionClient: ActivityRecognitionClient = ActivityRecognition.getClient(context)
    private lateinit var activityTransitionPendingIntent: PendingIntent

    init {
        initPendingIntent()
    }

    private fun initPendingIntent() {
        val intent = Intent(context, ActivityTransitionReceiver::class.java)
        activityTransitionPendingIntent = PendingIntent.getBroadcast(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    /**
     * Start monitoring activity transitions
     */
    fun startActivityRecognition() {
        // Check for the required permission
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(activityRecognitionTag, "Activity recognition permission not granted")
            return
        }

        // Set up activity transitions to monitor
        val transitions = listOf(
            // Detect when the user becomes still
            createActivityTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            // Detect when the user is no longer still
            createActivityTransition(DetectedActivity.STILL, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
            // Detect when the user starts driving
            createActivityTransition(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_ENTER),
            // Detect when the user stops driving
            createActivityTransition(DetectedActivity.IN_VEHICLE, ActivityTransition.ACTIVITY_TRANSITION_EXIT),
            // Detect when the user starts walking
            createActivityTransition(DetectedActivity.WALKING, ActivityTransition.ACTIVITY_TRANSITION_ENTER)
        )

        val request = ActivityTransitionRequest(transitions)

        activityRecognitionClient.requestActivityTransitionUpdates(request, activityTransitionPendingIntent)
            .addOnSuccessListener {
                Log.d(activityRecognitionTag, "Activity recognition registered successfully")
            }
            .addOnFailureListener { e ->
                Log.e(activityRecognitionTag, "Activity recognition registration failed: ${e.message}")
            }
    }

    /**
     * Stop monitoring activity transitions
     */
    fun stopActivityRecognition() {
        if (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACTIVITY_RECOGNITION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        activityRecognitionClient.removeActivityTransitionUpdates(activityTransitionPendingIntent)
            .addOnSuccessListener {
                Log.d(activityRecognitionTag, "Activity recognition unregistered successfully")
            }
            .addOnFailureListener { e ->
                Log.e(activityRecognitionTag, "Activity recognition unregistration failed: ${e.message}")
            }
    }

    private fun createActivityTransition(activityType: Int, transitionType: Int): ActivityTransition {
        return ActivityTransition.Builder()
            .setActivityType(activityType)
            .setActivityTransition(transitionType)
            .build()
    }
}