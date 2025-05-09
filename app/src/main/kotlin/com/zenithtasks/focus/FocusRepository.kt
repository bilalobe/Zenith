package com.zenithtasks.focus

import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.time.Duration
import java.time.LocalDateTime

/**
 * Represents the current state of focus mode
 */
sealed class FocusState {
    /** Focus mode is not active */
    object Inactive : FocusState()
    
    /** Focus mode is active */
    data class Active(
        val startTime: LocalDateTime,
        val duration: Duration?, // null means indefinite
        val activityType: FocusActivityType? = null
    ) : FocusState() {
        /**
         * Calculates the remaining time in milliseconds for the focus session
         * Returns 0 if the session has expired or -1 if duration is indefinite (null)
         */
        val remainingTimeMillis: Long
            get() {
                if (duration == null) return -1 // Indefinite duration
                val endTime = startTime.plus(duration)
                val now = LocalDateTime.now()
                if (now.isAfter(endTime)) return 0 // Session has expired
                return Duration.between(now, endTime).toMillis().coerceAtLeast(0)
            }
    }
}

/**
 * Types of activities that can be associated with focus mode
 */
enum class FocusActivityType {
    STILL, // User is not moving (e.g., deep work, studying)
    DRIVING, // User is in a vehicle
    WALKING, // User is walking
    UNKNOWN;
    
    companion object {
        fun fromActivityString(activity: String?): FocusActivityType {
            return when (activity?.lowercase()) {
                "still" -> STILL
                "in_vehicle" -> DRIVING
                "on_foot", "walking" -> WALKING
                else -> UNKNOWN
            }
        }
    }
}

/**
 * Repository interface for managing focus state and sessions
 */
interface FocusRepository {
    /**
     * The current focus state as a Flow that emits updates
     */
    val focusState: Flow<FocusState>
    
    /**
     * Start a focus session with the specified duration
     * @param durationMinutes Duration of the focus session in minutes
     */
    suspend fun startFocus(durationMinutes: Long)
    
    /**
     * Stop the current focus session
     */
    suspend fun stopFocus()
    
    /**
     * Get the current focus state
     * @return Current focus state
     */
    suspend fun getCurrentFocusState(): FocusState
    
    /**
     * Checks if system's Do Not Disturb (DND) mode is currently active
     */
    fun isSystemDndActive(): Boolean
    
    /**
     * Opens system's Do Not Disturb settings
     */
    fun openSystemDndSettings()
    
    /**
     * Opens system's Digital Wellbeing settings if available
     */
    fun openDigitalWellbeingSettings()
}

/**
 * Repository for managing focus mode state and interactions with system focus mode
 */
class FocusRepositoryImpl(private val context: Context) : FocusRepository {
    
    // Flow to provide current focus state
    private val _focusState = MutableStateFlow<FocusState>(FocusState.Inactive)
    override val focusState = _focusState.asStateFlow()
    
    /**
     * Checks if system's Do Not Disturb (DND) mode is currently active
     */
    override fun isSystemDndActive(): Boolean {
        val notificationManager = NotificationManagerCompat.from(context)
        return notificationManager.currentInterruptionFilter != 
               NotificationManagerCompat.INTERRUPTION_FILTER_ALL
    }
    
    /**
     * Opens system's Do Not Disturb settings
     */
    override fun openSystemDndSettings() {
        val intent = Intent("android.settings.ZEN_MODE_SETTINGS")
        try {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(intent)
        } catch (e: Exception) {
            android.util.Log.e("FocusRepositoryImpl", "Could not open DND settings", e)
        }
    }
    
    /**
     * Opens system's Digital Wellbeing settings if available
     */
    override fun openDigitalWellbeingSettings() {
        // Different manufacturers might use different intents for Digital Wellbeing
        val possibleIntents = listOf(
            Intent("android.settings.DIGITAL_WELLBEING_SETTINGS"),
            Intent("android.settings.ZEN_MODE_SETTINGS")
        )
        
        for (intent in possibleIntents) {
            try {
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                context.startActivity(intent)
                return
            } catch (e: Exception) {
                // Try the next intent if this one fails
                android.util.Log.w("FocusRepositoryImpl", "Intent failed, trying next: ${intent.action}", e)
            }
        }
        android.util.Log.e("FocusRepositoryImpl", "Could not open Digital Wellbeing settings")
    }

    override suspend fun startFocus(durationMinutes: Long) {
        val startTime = LocalDateTime.now()
        val duration = Duration.ofMinutes(durationMinutes)
        _focusState.value = FocusState.Active(startTime, duration)
    }

    override suspend fun stopFocus() {
        _focusState.value = FocusState.Inactive
    }

    override suspend fun getCurrentFocusState(): FocusState {
        return _focusState.value
    }
}