package com.zenithtasks.data.model

import com.google.firebase.firestore.DocumentSnapshot
import com.zenithtasks.data.local.entity.EnergyLevel
import java.util.Date

/**
 * Convert a Room Task entity to a map for Firestore storage
 */
fun Task.toFirestoreMap(): Map<String, Any?> {
    return mapOf(
        "id" to id,
        "title" to title,
        "description" to description,
        "isCompleted" to isCompleted,
        "createdAt" to createdAt,
        "updatedAt" to updatedAt,
        "dueDate" to dueDate,
        "locationId" to locationId,
        "locationReminderEnabled" to locationReminderEnabled
    )
}

/**
 * Convert a Firestore document to a Room Task entity
 */
fun DocumentSnapshot.toTask(): Task? {
    return try {
        val id = getLong("id") ?: return null
        val title = getString("title") ?: return null
        
        // Create default energy level and priority
        val energyLevel = try {
            val energyString = getString("energyLevel") ?: "MEDIUM"
            EnergyLevel.valueOf(energyString)
        } catch (e: Exception) {
            EnergyLevel.MEDIUM
        }
        
        val priority = try {
            val priorityString = getString("priority") ?: "LOW"
            TaskPriority.valueOf(priorityString)
        } catch (e: Exception) {
            TaskPriority.LOW
        }

        Task(
            id = id,
            title = title,
            description = getString("description"),
            isCompleted = getBoolean("isCompleted") == true,
            dueDate = getDate("dueDate"),
            createdAt = getDate("createdAt") ?: Date(),
            updatedAt = getDate("updatedAt") ?: Date(),
            locationId = getLong("locationId"),
            locationReminderEnabled = getBoolean("locationReminderEnabled") == true,
            completedDate = getDate("completedDate") ?: Date(),
            reminderTriggered = getBoolean("reminderTriggered") == true,
            firebaseId = id.toString(),
            lastSyncedAt = Date(),
            reminder = getDate("reminder"),
            pendingSync = false,
            isArchived = getBoolean("isArchived") == true,
            energyLevel = energyLevel,
            priority = priority
        )
    } catch (e: Exception) {
        null
    }
}

/**
 * Convert a Firestore document to a Room Location entity
 */
fun DocumentSnapshot.toLocation(): Location? {
    return try {
        val id = getLong("id") ?: return null
        val name = getString("name") ?: return null
        val latitude = getDouble("latitude") ?: return null
        val longitude = getDouble("longitude") ?: return null
        
        Location(
            id = id,
            name = name,
            latitude = latitude,
            longitude = longitude,
            radius = getDouble("radius")?.toFloat() ?: 100f,
            address = getString("address")
        )
    } catch (e: Exception) {
        null
    }
}
