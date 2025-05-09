package com.zenithtasks.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.util.Log
import com.zenithtasks.data.repository.FirebaseSyncRepository
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

/**
 * States representing the current sync status
 */
enum class SyncStatus {
    IDLE,       // No sync activity
    SYNCING,    // Currently syncing
    SUCCESS,    // Last sync was successful
    FAILED,     // Last sync failed
    OFFLINE     // Device is offline
}

/**
 * Manager class that coordinates synchronization between local database and Firebase.
 */
@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val firebaseSyncRepository: FirebaseSyncRepository
) {
    private val TAG = "SyncManager"
    
    private val job = SupervisorJob()
    private val scope = CoroutineScope(Dispatchers.IO + job)
    
    // Current sync status
    private val _syncStatus = MutableStateFlow(SyncStatus.IDLE)
    val syncStatus: StateFlow<SyncStatus> = _syncStatus.asStateFlow()
    
    /**
     * Perform a full synchronization of tasks and locations.
     * @return true if sync was successful, false otherwise
     */
    fun syncAll(): Boolean {
        if (!isNetworkAvailable()) {
            _syncStatus.value = SyncStatus.OFFLINE
            return false
        }
        
        if (!firebaseSyncRepository.isUserSignedIn()) {
            Log.d(TAG, "Cannot sync - user not signed in")
            return false
        }
        
        _syncStatus.value = SyncStatus.SYNCING
        
        var success = true
        scope.launch {
            try {
                // Sync tasks
                firebaseSyncRepository.syncTasks().collectLatest { taskSyncSuccess ->
                    if (!taskSyncSuccess) {
                        success = false
                    }
                }
                
                // Sync locations
                firebaseSyncRepository.syncLocations().collectLatest { locationSyncSuccess ->
                    if (!locationSyncSuccess) {
                        success = false
                    }
                }
                
                _syncStatus.value = if (success) SyncStatus.SUCCESS else SyncStatus.FAILED
            } catch (e: Exception) {
                Log.e(TAG, "Error during sync", e)
                _syncStatus.value = SyncStatus.FAILED
                success = false
            }
        }
        
        return success
    }
    
    /**
     * Sync only tasks
     * @return true if sync was initiated, false otherwise
     */
    fun syncTasks(): Boolean {
        if (!isNetworkAvailable()) {
            _syncStatus.value = SyncStatus.OFFLINE
            return false
        }
        
        if (!firebaseSyncRepository.isUserSignedIn()) {
            Log.d(TAG, "Cannot sync tasks - user not signed in")
            return false
        }
        
        _syncStatus.value = SyncStatus.SYNCING
        
        scope.launch {
            try {
                firebaseSyncRepository.syncTasks().collectLatest { success ->
                    _syncStatus.value = if (success) SyncStatus.SUCCESS else SyncStatus.FAILED
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during task sync", e)
                _syncStatus.value = SyncStatus.FAILED
            }
        }
        
        return true
    }
    
    /**
     * Sync only locations
     * @return true if sync was initiated, false otherwise
     */
    fun syncLocations(): Boolean {
        if (!isNetworkAvailable()) {
            _syncStatus.value = SyncStatus.OFFLINE
            return false
        }
        
        if (!firebaseSyncRepository.isUserSignedIn()) {
            Log.d(TAG, "Cannot sync locations - user not signed in")
            return false
        }
        
        _syncStatus.value = SyncStatus.SYNCING
        
        scope.launch {
            try {
                firebaseSyncRepository.syncLocations().collectLatest { success ->
                    _syncStatus.value = if (success) SyncStatus.SUCCESS else SyncStatus.FAILED
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error during location sync", e)
                _syncStatus.value = SyncStatus.FAILED
            }
        }
        
        return true
    }
    
    /**
     * Sign in with email and password
     * @param email User's email
     * @param password User's password
     * @return true if sign-in was initiated, false otherwise
     */
    fun signIn(email: String, password: String): Boolean {
        if (!isNetworkAvailable()) {
            return false
        }
        
        scope.launch {
            firebaseSyncRepository.signInWithEmailPassword(email, password)
        }
        
        return true
    }
    
    /**
     * Create a new account
     * @param email User's email
     * @param password User's password
     * @return true if account creation was initiated, false otherwise
     */
    fun createAccount(email: String, password: String): Boolean {
        if (!isNetworkAvailable()) {
            return false
        }
        
        scope.launch {
            firebaseSyncRepository.createAccount(email, password)
        }
        
        return true
    }
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        firebaseSyncRepository.signOut()
    }
    
    /**
     * Check if a network connection is available
     * @return true if a network connection is available, false otherwise
     */
    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val network = connectivityManager.activeNetwork ?: return false
        val capabilities = connectivityManager.getNetworkCapabilities(network) ?: return false
        
        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
    }
    
    /**
     * Check if user is currently signed in
     * @return true if user is signed in, false otherwise
     */
    fun isUserSignedIn(): Boolean = firebaseSyncRepository.isUserSignedIn()
    
    /**
     * Get the current user's email
     * @return User's email, or null if not signed in
     */
    fun getCurrentUserEmail(): String? = firebaseSyncRepository.currentUser?.email
    
    /**
     * Clean up resources when no longer needed
     */
    fun cleanup() {
        job.cancel()
    }
}