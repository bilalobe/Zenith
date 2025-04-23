package com.zenithtasks.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a location that can be associated with tasks.
 * These are predefined locations like Home, Work, Gym, Groceries.
 */
@Entity(tableName = "locations")
data class Location(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    // Name of the location (e.g., "Home", "Work", "Gym", "Groceries")
    val name: String,
    
    // Latitude and longitude of the location
    val latitude: Double,
    
    val longitude: Double,
    
    // Radius in meters for geofencing (default 100m)
    val radius: Float = 100f,
    
    // Optional address for display purposes
    val address: String? = null
)