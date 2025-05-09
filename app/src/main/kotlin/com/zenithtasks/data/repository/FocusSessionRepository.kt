package com.zenithtasks.data.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.zenithtasks.data.local.dao.FocusSessionDao
import com.zenithtasks.data.model.FocusSession
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.Date
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for managing focus sessions.
 * Provides an abstraction layer over the FocusSessionDao.
 */
@Singleton
class FocusSessionRepository @Inject constructor(
    private val focusSessionDao: FocusSessionDao,
    private val firestore: FirebaseFirestore,
    private val auth: FirebaseAuth
) {
    private val TAG = "FocusSessionRepository"

    /**
     * Get all focus sessions as a Flow.
     * @return Flow of all focus sessions, ordered by creation date (newest first).
     */
    fun getAllFocusSessions(): Flow<List<FocusSession>> {
        return focusSessionDao.getAllFocusSessions()
    }

    /**
     * Get all focus sessions as a List.
     * @return List of all focus sessions, ordered by creation date (newest first).
     */
    suspend fun getAllFocusSessionsList(): List<FocusSession> {
        return focusSessionDao.getAllFocusSessionsList()
    }

    /**
     * Get a focus session by its ID.
     * @param id The ID of the focus session to retrieve.
     * @return The focus session with the specified ID, or null if not found.
     */
    suspend fun getFocusSessionById(id: Long): FocusSession? {
        return focusSessionDao.getFocusSessionById(id)
    }

    /**
     * Get the currently active focus session, if any.
     * @return Flow with the currently active focus session, or null if none is active.
     */
    fun getActiveFocusSession(): Flow<FocusSession?> {
        return focusSessionDao.getActiveFocusSession()
    }

    /**
     * Start a new focus session.
     * @param startTime The start time of the focus session.
     * @param durationMinutes Optional duration in minutes for the session. If null, session is indefinite.
     * @return The ID of the newly created focus session.
     */
    suspend fun startFocusSession(startTime: Date, durationMinutes: Long? = null): Long {
        val newSession = FocusSession(
            startTime = startTime,
            isActive = true,
            pendingSync = true,
            createdAt = Date(),
            updatedAt = Date(),
            duration = durationMinutes
        )
        val localId = withContext(Dispatchers.IO) {
            focusSessionDao.insertFocusSession(newSession)
        }
        triggerSync()
        return localId
    }

    /**
     * End a focus session.
     * @param session The session to end.
     * @param endTime The end time of the session.
     */
    suspend fun endFocusSession(session: FocusSession, endTime: Date) {
        val durationMillis = endTime.time - session.startTime.time
        val durationMinutes = durationMillis / (1000 * 60)

        val updatedSession = session.copy(
            endTime = endTime,
            isActive = false,
            duration = durationMinutes,
            pendingSync = true
        )

        withContext(Dispatchers.IO) {
            focusSessionDao.insertFocusSession(updatedSession)
        }
        triggerSync()
    }

    /**
     * Delete a focus session.
     * @param focusSession The focus session to delete.
     */
    suspend fun deleteFocusSession(focusSession: FocusSession) {
        focusSessionDao.deleteFocusSession(focusSession)
    }

    /**
     * Delete a focus session by its ID.
     * @param id The ID of the focus session to delete.
     */
    suspend fun deleteFocusSessionById(id: Long) {
        focusSessionDao.deleteFocusSessionById(id)
    }

    /**
     * Get all focus sessions that need to be synced with Firebase.
     * @return List of focus sessions with pendingSync = true.
     */
    suspend fun getFocusSessionsPendingSync(): List<FocusSession> {
        return focusSessionDao.getFocusSessionsPendingSync()
    }

    /**
     * Mark a focus session as synced.
     * @param id The ID of the focus session to mark as synced.
     */
    suspend fun markFocusSessionSynced(id: Long) {
        focusSessionDao.markFocusSessionSynced(id, Date())
    }

    /**
     * Update a focus session's Firebase ID.
     * @param id The ID of the focus session.
     * @param firebaseId The Firebase document ID to set.
     */
    suspend fun updateFocusSessionFirebaseId(id: Long, firebaseId: String) {
        focusSessionDao.updateFocusSessionFirebaseId(id, firebaseId)
    }

    /**
     * Triggers the sync process (push local changes, then fetch remote).
     */
    suspend fun triggerSync() {
        Log.d(TAG, "Sync triggered")
        pushPendingSessions()
    }

    /**
     * Pushes locally modified/created sessions to Firebase.
     */
    private suspend fun pushPendingSessions() {
        val userId = auth.currentUser?.uid ?: run {
            Log.w(TAG, "Cannot push sessions, user not logged in.")
            return
        }
        val sessionsToPush = withContext(Dispatchers.IO) {
            focusSessionDao.getFocusSessionsPendingSync()
        }

        if (sessionsToPush.isEmpty()) {
            Log.d(TAG, "No pending sessions to push.")
            return
        }

        Log.d(TAG, "Pushing ${sessionsToPush.size} pending sessions...")
        val userSessionsCollection = firestore.collection("users").document(userId).collection("focusSessions")

        withContext(Dispatchers.IO) {
            sessionsToPush.forEach { session ->
                try {
                    val sessionData = session.toMap()
                    if (session.firebaseId == null) {
                        val docRef = userSessionsCollection.add(sessionData).await()
                        Log.d(TAG, "Added new session to Firestore with ID: ${docRef.id}")
                        focusSessionDao.updateFocusSessionFirebaseId(session.id, docRef.id)
                        focusSessionDao.markFocusSessionSynced(session.id, Date())
                    } else {
                        userSessionsCollection.document(session.firebaseId).set(sessionData).await()
                        Log.d(TAG, "Updated session in Firestore: ${session.firebaseId}")
                        focusSessionDao.markFocusSessionSynced(session.id, Date())
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Error pushing session ${session.id} (Firebase ID: ${session.firebaseId})", e)
                }
            }
        }
        Log.d(TAG, "Finished pushing sessions.")
    }
}

/**
 * Converts FocusSession to a Map suitable for Firestore.
 */
fun FocusSession.toMap(): Map<String, Any?> {
    return mapOf(
        "startTime" to startTime,
        "endTime" to endTime,
        "duration" to duration,
        "isActive" to isActive
    )
}