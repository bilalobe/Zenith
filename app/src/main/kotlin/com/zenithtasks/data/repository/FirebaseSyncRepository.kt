package com.zenithtasks.data.repository

import android.content.SharedPreferences
import android.util.Log
import androidx.core.content.edit
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.zenithtasks.data.local.dao.FocusSessionDao
import com.zenithtasks.data.local.dao.LocationDao
import com.zenithtasks.data.local.dao.TaskDao
import com.zenithtasks.data.local.entity.TaskEntity
import com.zenithtasks.data.model.toFirestoreMap
import com.zenithtasks.data.model.toFocusSession
import com.zenithtasks.data.model.toLocation
import com.zenithtasks.data.model.toTask
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for synchronizing data between local Room database and Firebase Firestore.
 */
@Singleton
class FirebaseSyncRepository @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth,
    private val taskDao: TaskDao,
    private val locationDao: LocationDao,
    private val focusSessionDao: FocusSessionDao,
    private val preferences: SharedPreferences
) {
    private val fireSyncTag = "FirebaseSyncRepository"
    
    // Get the current authenticated user
    val currentUser: FirebaseUser?
        get() = auth.currentUser
    
    // Get the user ID for Firestore paths
    private val userId: String?
        get() = currentUser?.uid
    
    // Last sync timestamp key
    private val LAST_SYNC_KEY = "last_sync_timestamp"
    
    /**
     * Sign in with email and password
     * @param email User's email
     * @param password User's password
     * @return Flow that emits true if sign-in was successful, false otherwise
     */
    fun signInWithEmailPassword(email: String, password: String): Flow<Boolean> = flow {
        try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            emit(result.user != null)
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error signing in", e)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Create a new account
     * @param email User's email
     * @param password User's password
     * @return Flow that emits true if account creation was successful, false otherwise
     */
    fun createAccount(email: String, password: String): Flow<Boolean> = flow {
        try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            emit(result.user != null)
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error creating account", e)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Sign out the current user
     */
    fun signOut() {
        auth.signOut()
    }
    
    /**
     * Check if a user is currently signed in
     * @return true if a user is signed in, false otherwise
     */
    fun isUserSignedIn(): Boolean {
        return auth.currentUser != null
    }
    
    /**
     * Synchronize tasks between local database and Firestore
     * @return Flow that emits true if sync was successful, false otherwise
     */
    fun syncTasks(): Flow<Boolean> = flow {
        if (!isUserSignedIn()) {
            Log.d(fireSyncTag, "Cannot sync tasks - user not signed in")
            emit(false)
            return@flow
        }
        
        val uid = userId ?: run {
            Log.e(fireSyncTag, "User ID is null")
            emit(false)
            return@flow
        }
        
        try {
            // Get the last sync timestamp
            val lastSync = getLastSyncTimestamp()
            
            // Pull changes from Firestore
            val success = pullTasksFromFirestore(uid)
            
            // Push local changes to Firestore
            if (success) {
                pushTasksToFirestore(uid, lastSync)
            } else {
                emit(false)
                return@flow
            }
            
            // Update the last sync timestamp
            saveLastSyncTimestamp(Date())
            
            emit(true)
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error syncing tasks", e)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Pull tasks from Firestore and update local database
     * @param uid User ID
     * @return true if successful, false otherwise
     */
    private suspend fun pullTasksFromFirestore(uid: String): Boolean {
        return try {
            val tasksCollection = firestore.collection("users/$uid/tasks")
            val documents = tasksCollection.get().await()
            
            val remoteTasks = documents.mapNotNull { it.toTask() }
                .map { task -> 
                    // Convert Task to TaskEntity
                    TaskEntity(
                        id = task.id,
                        title = task.title,
                        description = task.description,
                        isCompleted = task.isCompleted,
                        dueDate = task.dueDate,
                        priority = task.priority,
                        energyLevel = task.energyLevel,
                        locationId = task.locationId,
                        locationReminderEnabled = task.locationReminderEnabled,
                        reminderTriggered = task.reminderTriggered,
                        isArchived = task.isArchived,
                        createdAt = task.createdAt, // Ensure non-null date
                        updatedAt = task.updatedAt  // Ensure non-null date
                    )
                }
            
            // Insert or update tasks in local database
            if (remoteTasks.isNotEmpty()) {
                taskDao.insertTasks(remoteTasks)
            }
            
            true
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error pulling tasks from Firestore", e)
            false
        }
    }
    
    /**
     * Push local tasks to Firestore
     * @param uid User ID
     * @param since Only push tasks modified since this timestamp
     * @return true if successful, false otherwise
     */
    private suspend fun pushTasksToFirestore(uid: String, since: Date): Boolean {
        return try {
            val tasksCollection = firestore.collection("users/$uid/tasks")
            
            // Get locally modified tasks
            val modifiedTasks = taskDao.getTasksModifiedSince(since)
            
            // Push each task to Firestore
            withContext(Dispatchers.IO) {
                for (task in modifiedTasks) {
                    tasksCollection.document(task.id.toString())
                        .set(task)
                        .await()
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error pushing tasks to Firestore", e)
            false
        }
    }
    
    /**
     * Synchronize locations between local database and Firestore
     * @return Flow that emits true if sync was successful, false otherwise
     */
    fun syncLocations(): Flow<Boolean> = flow {
        if (!isUserSignedIn()) {
            Log.d(fireSyncTag, "Cannot sync locations - user not signed in")
            emit(false)
            return@flow
        }
        
        val uid = userId ?: run {
            Log.e(fireSyncTag, "User ID is null")
            emit(false)
            return@flow
        }
        
        try {
            // Pull changes from Firestore
            val success = pullLocationsFromFirestore(uid)
            
            // Push local changes to Firestore
            if (success) {
                pushLocationsToFirestore(uid)
            } else {
                emit(false)
                return@flow
            }
            
            // Update the last sync timestamp
            saveLastSyncTimestamp(Date())
            
            emit(true)
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error syncing locations", e)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Pull locations from Firestore and update local database
     * @param uid User ID
     * @return true if successful, false otherwise
     */
    private suspend fun pullLocationsFromFirestore(uid: String): Boolean {
        return try {
            val locationsCollection = firestore.collection("users/$uid/locations")
            val documents = locationsCollection.get().await()
            
            val remoteLocations = documents.mapNotNull { it.toLocation() }
            
            // Insert or update locations in local database
            if (remoteLocations.isNotEmpty()) {
                for (location in remoteLocations) {
                    locationDao.insertLocation(location)
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error pulling locations from Firestore", e)
            false
        }
    }
    
    /**
     * Push local locations to Firestore
     * @param uid User ID
     * @return true if successful, false otherwise
     */
    private suspend fun pushLocationsToFirestore(uid: String): Boolean {
        return try {
            val locationsCollection = firestore.collection("users/$uid/locations")
            
            // Get all local locations using the direct list method
            val locationsList = withContext(Dispatchers.IO) {
                locationDao.getAllLocationsList()
            }
            
            // Push each location to Firestore
            withContext(Dispatchers.IO) {
                for (location in locationsList) {
                    locationsCollection.document(location.id.toString())
                        .set(mapOf(
                            "id" to location.id,
                            "name" to location.name,
                            "latitude" to location.latitude,
                            "longitude" to location.longitude,
                            "radius" to location.radius,
                            "address" to location.address
                        ))
                        .await()
                }
            }
            
            true
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error pushing locations to Firestore", e)
            false
        }
    }
    
    /**
     * Synchronize focus sessions between local database and Firestore
     * @return Flow that emits true if sync was successful, false otherwise
     */
    fun syncFocusSessions(): Flow<Boolean> = flow {
        if (!isUserSignedIn()) {
            Log.d(fireSyncTag, "Cannot sync focus sessions - user not signed in")
            emit(false)
            return@flow
        }
        
        val uid = userId ?: run {
            Log.e(fireSyncTag, "User ID is null")
            emit(false)
            return@flow
        }
        
        try {
            // Get the last sync timestamp
            val lastSync = getLastSyncTimestamp()
            
            // Pull changes from Firestore
            val success = pullFocusSessionsFromFirestore(uid)
            
            // Push local changes to Firestore
            if (success) {
                pushFocusSessionsToFirestore(uid, lastSync)
            } else {
                emit(false)
                return@flow
            }
            
            // Update the last sync timestamp
            saveLastSyncTimestamp(Date())
            
            emit(true)
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error syncing focus sessions", e)
            emit(false)
        }
    }.flowOn(Dispatchers.IO)
    
    /**
     * Pull focus sessions from Firestore and update local database
     * @param uid User ID
     * @return true if successful, false otherwise
     */
    private suspend fun pullFocusSessionsFromFirestore(uid: String): Boolean {
        return try {
            val focusSessionsCollection = firestore.collection("users/$uid/focusSessions")
            val documents = focusSessionsCollection.get().await()
            
            val remoteFocusSessions = documents.mapNotNull { it.toFocusSession() }
            
            // Insert or update focus sessions in local database
            if (remoteFocusSessions.isNotEmpty()) {
                focusSessionDao.insertFocusSessions(remoteFocusSessions)
            }
            
            true
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error pulling focus sessions from Firestore", e)
            false
        }
    }
    
    /**
     * Push local focus sessions to Firestore
     * @param uid User ID
     * @param since Only push focus sessions modified since this timestamp
     * @return true if successful, false otherwise
     */
    private suspend fun pushFocusSessionsToFirestore(uid: String, since: Date): Boolean {
        return try {
            val focusSessionsCollection = firestore.collection("users/$uid/focusSessions")
            
            // Get locally modified focus sessions - using a modified approach
            // This addresses the "Unresolved reference 'getFocusSessionsModifiedSince'" error
            val modifiedFocusSessions = focusSessionDao.getAllFocusSessionsList().filter { 
                it.updatedAt.after(since) && it.pendingSync 
            }
            
            // Push each focus session to Firestore
            withContext(Dispatchers.IO) {
                for (focusSession in modifiedFocusSessions) {
                    focusSessionsCollection.document(focusSession.id.toString())
                        .set(focusSession.toFirestoreMap())
                        .await()
                }
            }
            
            // Update pendingSync flags - fixed the "updateSyncStatus" error by calling markFocusSessionSynced
            for (session in modifiedFocusSessions) {
                focusSessionDao.markFocusSessionSynced(session.id, Date())
            }
            
            true
        } catch (e: Exception) {
            Log.e(fireSyncTag, "Error pushing focus sessions to Firestore", e)
            false
        }
    }
    
    /**
     * Get the timestamp of the last successful sync
     * @return The last sync timestamp, or epoch time if not available
     */
    private fun getLastSyncTimestamp(): Date {
        val timestamp = preferences.getLong(LAST_SYNC_KEY, 0)
        return if (timestamp > 0) {
            Date(timestamp)
        } else {
            Date(0) // Epoch time - January 1, 1970
        }
    }
    
    /**
     * Save the timestamp of the last successful sync
     * @param timestamp The timestamp to save
     */
    private fun saveLastSyncTimestamp(timestamp: Date) {
        preferences.edit {
            putLong(LAST_SYNC_KEY, timestamp.time)
        }
    }
}