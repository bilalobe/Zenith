package com.zenithtasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenithtasks.data.model.FocusSession
import kotlinx.coroutines.flow.Flow
import java.util.Date

/**
 * Data Access Object for the FocusSession entity.
 * Provides methods to interact with the focus_sessions table in the database.
 */
@Dao
interface FocusSessionDao {

    /**
     * Get all focus sessions as a Flow.
     */
    @Query("SELECT * FROM focus_sessions ORDER BY createdAt DESC")
    fun getAllFocusSessions(): Flow<List<FocusSession>>

    /**
     * Get all focus sessions as a list.
     */
    @Query("SELECT * FROM focus_sessions ORDER BY createdAt DESC")
    suspend fun getAllFocusSessionsList(): List<FocusSession>

    /**
     * Get a focus session by its ID.
     */
    @Query("SELECT * FROM focus_sessions WHERE id = :id")
    suspend fun getFocusSessionById(id: Long): FocusSession?

    /**
     * Get the currently active focus session, if any.
     */
    @Query("SELECT * FROM focus_sessions WHERE isActive = 1 LIMIT 1")
    fun getActiveFocusSession(): Flow<FocusSession?>

    /**
     * Insert a new focus session into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSession(focusSession: FocusSession): Long

    /**
     * Insert multiple focus sessions into the database.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFocusSessions(focusSessions: List<FocusSession>): List<Long>

    /**
     * Update an existing focus session in the database.
     */
    @Update
    suspend fun updateFocusSession(focusSession: FocusSession)

    /**
     * End a focus session.
     */
    @Query("UPDATE focus_sessions SET endTime = :endTime, duration = :durationMinutes, isActive = 0, updatedAt = :endTime, pendingSync = 1 WHERE id = :id")
    suspend fun endFocusSession(id: Long, endTime: Date, durationMinutes: Long)

    /**
     * Delete a focus session from the database.
     */
    @Delete
    suspend fun deleteFocusSession(focusSession: FocusSession)

    /**
     * Delete a focus session by its ID.
     */
    @Query("DELETE FROM focus_sessions WHERE id = :id")
    suspend fun deleteFocusSessionById(id: Long)

    /**
     * Get all focus sessions that need to be synced with Firebase.
     */
    @Query("SELECT * FROM focus_sessions WHERE pendingSync = 1")
    suspend fun getFocusSessionsPendingSync(): List<FocusSession>

    /**
     * Mark a focus session as synced.
     */
    @Query("UPDATE focus_sessions SET pendingSync = 0, lastSyncedAt = :lastSyncedAt WHERE id = :id")
    suspend fun markFocusSessionSynced(id: Long, lastSyncedAt: Date)

    /**
     * Update a focus session's Firebase ID.
     * @param id The ID of the focus session.
     * @param firebaseId The Firebase document ID to set.
     */
    @Query("UPDATE focus_sessions SET firebaseId = :firebaseId WHERE id = :id")
    suspend fun updateFocusSessionFirebaseId(id: Long, firebaseId: String)
}