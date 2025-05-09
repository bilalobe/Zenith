package com.zenithtasks.focus

import android.app.Service
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.os.IBinder
import android.service.quicksettings.TileService
import android.util.Log
import com.zenithtasks.data.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import java.util.Date
import javax.inject.Inject

@AndroidEntryPoint
class FocusControlService : Service() {

    @Inject
    lateinit var focusSessionRepository: FocusSessionRepository

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.IO + serviceJob)

    private val TAG = "FocusControlService"

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(TAG, "onStartCommand received: ${intent?.action}")
        when (intent?.action) {
            ACTION_TOGGLE_FOCUS -> handleToggleFocus()
            ACTION_START_FOCUS -> {
                val durationMinutes = intent.getLongExtra(EXTRA_DURATION_MINUTES, 25L)
                handleStartFocus(durationMinutes)
            }
            ACTION_STOP_FOCUS -> handleStopFocus()
            // Add other actions if needed
        }
        // Stop self if no more work is needed after handling the action
        // Use START_NOT_STICKY as we only need to handle the command
        return START_NOT_STICKY
    }

    private fun handleToggleFocus() {
        serviceScope.launch {
            try {
                val currentSession = focusSessionRepository.getActiveFocusSession().firstOrNull()
                if (currentSession == null) {
                    Log.d(TAG, "Starting focus session...")
                    focusSessionRepository.startFocusSession(Date())
                } else {
                    Log.d(TAG, "Stopping focus session: ${currentSession.id}")
                    focusSessionRepository.endFocusSession(currentSession, Date())
                }
                // Request the tile service to update its state
                requestTileUpdate(this@FocusControlService)
            } catch (e: Exception) {
                Log.e(TAG, "Error toggling focus session", e)
            } finally {
                stopSelf() // Stop the service once the toggle is done
            }
        }
    }

    private fun handleStartFocus(durationMinutes: Long) {
        serviceScope.launch {
            try {
                val currentSession = focusSessionRepository.getActiveFocusSession().firstOrNull()
                if (currentSession == null) {
                    Log.d(TAG, "Starting focus session for $durationMinutes minutes...")
                    focusSessionRepository.startFocusSession(Date(), durationMinutes)
                } else {
                    Log.d(TAG, "Focus session already active, not starting a new one")
                }
                // Request the tile service to update its state
                requestTileUpdate(this@FocusControlService)
            } catch (e: Exception) {
                Log.e(TAG, "Error starting focus session", e)
            } finally {
                stopSelf() // Stop the service once done
            }
        }
    }

    private fun handleStopFocus() {
        serviceScope.launch {
            try {
                val currentSession = focusSessionRepository.getActiveFocusSession().firstOrNull()
                if (currentSession != null) {
                    Log.d(TAG, "Stopping focus session: ${currentSession.id}")
                    focusSessionRepository.endFocusSession(currentSession, Date())
                } else {
                    Log.d(TAG, "No active focus session to stop")
                }
                // Request the tile service to update its state
                requestTileUpdate(this@FocusControlService)
            } catch (e: Exception) {
                Log.e(TAG, "Error stopping focus session", e)
            } finally {
                stopSelf() // Stop the service once done
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        Log.d(TAG, "onDestroy")
    }

    override fun onBind(intent: Intent?): IBinder? {
        return null // Not a bound service
    }

    companion object {
        const val ACTION_TOGGLE_FOCUS = "com.zenithtasks.focus.ACTION_TOGGLE_FOCUS"
        const val ACTION_START_FOCUS = "com.zenithtasks.focus.ACTION_START_FOCUS"
        const val ACTION_STOP_FOCUS = "com.zenithtasks.focus.ACTION_STOP_FOCUS"
        const val EXTRA_DURATION_MINUTES = "com.zenithtasks.focus.EXTRA_DURATION_MINUTES"

        fun requestTileUpdate(context: Context) {
            Log.d("FocusControlService", "Requesting tile update")
            // TileService.requestListeningState is API 24+
            TileService.requestListeningState(
                context,
                ComponentName(context, FocusTileService::class.java)
            )
        }
    }
}