package com.zenithtasks.focus

import android.content.Intent
import android.graphics.drawable.Icon
import android.service.quicksettings.Tile
import android.service.quicksettings.TileService
import android.util.Log
import com.zenithtasks.R
import com.zenithtasks.data.repository.FocusSessionRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class FocusTileService : TileService() {

    @Inject
    lateinit var focusSessionRepository: FocusSessionRepository // Hilt injection

    private val serviceJob = SupervisorJob()
    private val serviceScope = CoroutineScope(Dispatchers.Main + serviceJob) // Use Main for UI updates

    private val TAG = "FocusTileService"

    override fun onStartListening() {
        super.onStartListening()
        Log.d(TAG, "onStartListening")
        // Observe the focus state and update the tile
        serviceScope.launch {
            focusSessionRepository.getActiveFocusSession().collectLatest { activeSession ->
                updateTile(activeSession != null)
            }
        }
    }

    private fun updateTile(isActive: Boolean) {
        val tile = qsTile ?: return // Get the tile object

        if (isActive) {
            tile.state = Tile.STATE_ACTIVE
            tile.label = getString(R.string.focus_tile_label_active) // e.g., "Focus On"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_focus_on) // Replace with your icon
        } else {
            tile.state = Tile.STATE_INACTIVE
            tile.label = getString(R.string.focus_tile_label_inactive) // e.g., "Focus Off"
            tile.icon = Icon.createWithResource(this, R.drawable.ic_tile_focus_off) // Replace with your icon
        }
        Log.d(TAG, "Updating tile state: ${tile.state}")
        tile.updateTile()
    }

    override fun onClick() {
        super.onClick()
        Log.d(TAG, "onClick")
        // Send command to FocusControlService to toggle the state
        val intent = Intent(this, FocusControlService::class.java).apply {
            action = FocusControlService.ACTION_TOGGLE_FOCUS
        }
        // Use startService for API < 26, startForegroundService for API >= 26
        startForegroundService(intent)
        // Optional: Unlock device if needed to perform action (requires permissions)
        // unlockAndRun { ... }
    }

    override fun onStopListening() {
        super.onStopListening()
        Log.d(TAG, "onStopListening")
        serviceJob.cancel() // Cancel coroutines when not listening
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy")
        serviceJob.cancel() // Ensure cancellation
    }
}