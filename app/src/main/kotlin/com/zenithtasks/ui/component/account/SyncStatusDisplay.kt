package com.zenithtasks.ui.component.account

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudDone
import androidx.compose.material.icons.filled.CloudQueue
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material.icons.filled.SyncProblem
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import com.zenithtasks.sync.SyncStatus
import com.zenithtasks.ui.util.Quad

@Composable
fun SyncStatusDisplay(
    syncStatus: SyncStatus,
    isSyncing: Boolean
) {
    if (isSyncing) {
        StatusRow(
            icon = Icons.Default.Sync,
            text = "Syncing...",
            description = "Your data is being synchronized",
            tint = MaterialTheme.colorScheme.primary
        )
        return
    }

    val (icon, text, description, tint) = when (syncStatus) {
        SyncStatus.SUCCESS -> Quad(
            Icons.Default.CloudDone,
            "Synced",
            "Your data is up to date",
            MaterialTheme.colorScheme.primary
        )
        SyncStatus.FAILED -> Quad(
            Icons.Default.SyncProblem,
            "Sync Failed",
            "Could not sync data. Check connection or try again.",
            MaterialTheme.colorScheme.error
        )
        SyncStatus.IDLE -> Quad(
            Icons.Default.CloudQueue,
            "Ready to Sync",
            "Tap 'Sync Now' to synchronize your data",
            MaterialTheme.colorScheme.secondary
        )
        else -> Quad(
            Icons.Default.CloudQueue,
            "Status Unknown",
            "",
            MaterialTheme.colorScheme.outline
        )
    }

    StatusRow(icon = icon, text = text, description = description, tint = tint)
}