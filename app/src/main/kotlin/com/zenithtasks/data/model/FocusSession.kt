package com.zenithtasks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

/**
 * Represents a focus/pomodoro session.
 * Tracks when a user starts and ends a focus session.
 */
@Entity(tableName = "focus_sessions")
data class FocusSession(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // When the focus session was started
    val startTime: Date,
    
    // When the focus session ended (null if still active)
    val endTime: Date? = null,
    
    // Duration of the session in minutes (calculated when session ends)
    val duration: Long? = null,
    
    // Whether the session is currently active
    val isActive: Boolean = true,
    
    // Creation timestamp (for sorting)
    val createdAt: Date = Date(),
    
    // Last updated timestamp (for sync)
    val updatedAt: Date = Date(),
    
    // Firebase document ID for this session
    val firebaseId: String? = null,
    
    // Whether this session needs to be synced to Firebase
    val pendingSync: Boolean = true,
    
    // When this session was last synced with Firebase
    val lastSyncedAt: Date? = null
)

/**
 * Convert a FocusSession to a map for Firestore storage
 */
fun FocusSession.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "startTime" to startTime,
        "endTime" to endTime,
        "duration" to duration,
        "isActive" to isActive,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt
    )
}

/**
 * Extension functions to convert a Firestore document to a FocusSession
 */
fun com.google.firebase.firestore.DocumentSnapshot.toFocusSession(): FocusSession? {
    return try {
        val id = getLong("id") ?: return null
        val startTime = getDate("startTime") ?: return null
        val endTime = getDate("endTime")
        val duration = getLong("duration")
        val isActive = getBoolean("isActive") != false
        val createdAt = getDate("createdAt") ?: Date()
        val updatedAt = getDate("updatedAt") ?: Date()
        
        FocusSession(
            id = id,
            startTime = startTime,
            endTime = endTime,
            duration = duration,
            isActive = isActive,
            createdAt = createdAt,
            updatedAt = updatedAt,
            firebaseId = id.toString(),
            pendingSync = false,
            lastSyncedAt = Date()
        )
    } catch (e: Exception) {
        null
    }
}