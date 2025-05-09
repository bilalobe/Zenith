package com.zenithtasks.location

import android.Manifest
import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.Geofence
import com.google.android.gms.location.GeofencingClient
import com.google.android.gms.location.GeofencingRequest
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.zenithtasks.MainActivity
import com.zenithtasks.R
import com.zenithtasks.data.local.db.AppDatabase
import com.zenithtasks.data.model.TaskWithLocation
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * Service for tracking user location and managing geofences.
 * This service runs in the foreground to ensure it's not killed by the system.
 */
class LocationService : Service() {

    private val tag = "LocationService"
    private val locationChannelId = "location_channel"
    private val foregroundServiceId = 1
    private val geofenceRadiusMeters = 100f
    private val geofenceExpirationMs = 24 * 60 * 60 * 1000L // 24 hours

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var geofencingClient: GeofencingClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var database: AppDatabase

    // Coroutine scope for background operations
    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    // Pending intent for geofence transitions
    private lateinit var geofencePendingIntent: PendingIntent

    override fun onCreate() {
        super.onCreate()
        Log.d(tag, "LocationService created")

        // Initialize location clients
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        geofencingClient = LocationServices.getGeofencingClient(this)
        database = AppDatabase.getInstance(this)

        // Create notification channel for foreground service
        createNotificationChannel()

        // Initialize location callback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    Log.d(tag, "Location update: ${location.latitude}, ${location.longitude}")
                    // Check if we're near any of our saved locations
                    checkNearbyLocations(location)
                }
            }
        }

        // Initialize geofence pending intent
        geofencePendingIntent = PendingIntent.getBroadcast(
            this,
            0,
            Intent(this, GeofenceBroadcastReceiver::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )
    }

    @SuppressLint("ForegroundServiceType")
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(tag, "LocationService started")

        // Start as a foreground service with notification
        val notification = NotificationCompat.Builder(this, locationChannelId)
            .setContentTitle("Zenith Tasks")
            .setContentText("Monitoring your location for task reminders")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        startForeground(foregroundServiceId, notification)

        // Request location updates
        requestLocationUpdates()

        // Set up geofences for all locations with tasks
        setupGeofences()

        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(tag, "LocationService destroyed")

        // Stop location updates
        fusedLocationClient.removeLocationUpdates(locationCallback)

        // Remove all geofences
        geofencingClient.removeGeofences(geofencePendingIntent)

        // Cancel all coroutines
        serviceJob.cancel()
    }

    /**
     * Creates a notification channel for the foreground service.
     */
    private fun createNotificationChannel() {
        val name = "Location Updates"
        val descriptionText = "Channel for location update notifications"
        val importance = NotificationManager.IMPORTANCE_LOW
        val channel = NotificationChannel(locationChannelId, name, importance).apply {
            description = descriptionText
        }
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.createNotificationChannel(channel)
    }

    /**
     * Requests location updates from the Fused Location Provider.
     */
    private fun requestLocationUpdates() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "Location permissions not granted")
            return
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_BALANCED_POWER_ACCURACY, 15 * 60 * 1000) // 15 minutes
            .setMinUpdateIntervalMillis(5 * 60 * 1000) // 5 minutes
            .build()

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            locationCallback,
            Looper.getMainLooper()
        )
    }

    /**
     * Sets up geofences for all locations with tasks.
     */
    private fun setupGeofences() {
        serviceScope.launch {
            try {
                // Get all tasks with location reminders enabled
                val tasksWithLocations = database.taskDao().getTasksWithLocationRemindersAndLocation()

                // Filter out tasks without locations
                val validTasksWithLocations = tasksWithLocations.filter { it.location != null }

                if (validTasksWithLocations.isEmpty()) {
                    Log.d(tag, "No tasks with location reminders found")
                    return@launch
                }

                // Add geofences for each location
                addGeofences(validTasksWithLocations)
            } catch (e: Exception) {
                Log.e(tag, "Error setting up geofences", e)
            }
        }
    }

    /**
     * Adds geofences for the given tasks with locations.
     *
     * @param tasksWithLocations List of tasks with their associated locations
     */
    private fun addGeofences(tasksWithLocations: List<TaskWithLocation>) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            Log.e(tag, "Fine location permission not granted")
            return
        }

        val geofenceList = tasksWithLocations.mapNotNull { taskWithLocation ->
            taskWithLocation.location?.let { location ->
                Geofence.Builder()
                    .setRequestId(location.id.toString())
                    .setCircularRegion(
                        location.latitude,
                        location.longitude,
                        geofenceRadiusMeters
                    )
                    .setExpirationDuration(geofenceExpirationMs)
                    .setTransitionTypes(Geofence.GEOFENCE_TRANSITION_ENTER)
                    .build()
            }
        }

        if (geofenceList.isEmpty()) {
            Log.d(tag, "No valid geofences to add")
            return
        }

        val geofencingRequest = GeofencingRequest.Builder()
            .setInitialTrigger(GeofencingRequest.INITIAL_TRIGGER_ENTER)
            .addGeofences(geofenceList)
            .build()

        try {
            geofencingClient.addGeofences(geofencingRequest, geofencePendingIntent)
            Log.d(tag, "Geofences added: ${geofenceList.size}")
        } catch (e: Exception) {
            Log.e(tag, "Failed to add geofences", e)
        }
    }

    /**
     * Checks if the current location is near any of the saved locations.
     *
     * @param currentLocation The current device location
     */
    private fun checkNearbyLocations(currentLocation: android.location.Location) {
        serviceScope.launch {
            try {
                // Get all locations from the database
                val locations = withContext(Dispatchers.IO) {
                    database.locationDao().getAllLocationsList()
                }

                // Check each location
                for (location in locations) {
                    val locationPoint = android.location.Location("").apply {
                        latitude = location.latitude
                        longitude = location.longitude
                    }

                    // Calculate distance to the location
                    val distance = currentLocation.distanceTo(locationPoint)

                    // If within the geofence radius, trigger notification
                    if (distance <= geofenceRadiusMeters) {
                        Log.d(tag, "Near location: ${location.name}, distance: $distance meters")

                        // Get tasks for this location
                        val tasks = withContext(Dispatchers.IO) {
                            database.taskDao().getTasksByLocationFlow(location.id).first()
                        }

                        // Process each task
                        for (task in tasks) {
                            if (!task.reminderTriggered && task.locationReminderEnabled && !task.isCompleted) {
                                // Mark reminder as triggered
                                withContext(Dispatchers.IO) {
                                    database.taskDao().markReminderAsTriggered(task.id)
                                }

                                // Show notification using NotificationHelper
                                val notificationHelper = NotificationHelper(this@LocationService)
                                notificationHelper.showTaskNotification(task.id, task.title, location.name)
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                Log.e(tag, "Error checking nearby locations", e)
            }
        }
    }

    /**
     * Shows a notification for a task at a location.
     *
     * @param taskId The ID of the task
     * @param taskTitle The title of the task
     * @param locationName The name of the location
     */
    private fun showTaskNotification(taskId: Long, taskTitle: String, locationName: String) {
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager

        // Create intent to open the task details
        val intent = Intent(this, MainActivity::class.java).apply {
            putExtra("taskId", taskId)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        val pendingIntent = PendingIntent.getActivity(
            this, taskId.toInt(), intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Build the notification
        val notification = NotificationCompat.Builder(this, locationChannelId)
            .setContentTitle("Task Reminder: $taskTitle")
            .setContentText("You're at $locationName")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        // Show the notification
        notificationManager.notify(taskId.toInt() + 100, notification)
    }

    companion object {
        /**
         * Starts the location service.
         *
         * @param context The context to start the service from
         */
        fun startService(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.startForegroundService(intent)
        }

        /**
         * Stops the location service.
         *
         * @param context The context to stop the service from
         */
        fun stopService(context: Context) {
            val intent = Intent(context, LocationService::class.java)
            context.stopService(intent)
        }
    }
}
