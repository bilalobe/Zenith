package com.zenithtasks.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenithtasks.data.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the Location entity.
 * Provides methods to interact with the locations table in the database.
 */
@Dao
interface LocationDao {
    
    /**
     * Get all locations as a Flow.
     * @return Flow of all locations in the database.
     */
    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocationsFlow(): Flow<List<Location>>
    
    /**
     * Get all locations as a list.
     * @return List of all locations in the database.
     */
    @Query("SELECT * FROM locations ORDER BY name ASC")
    suspend fun getAllLocations(): List<Location>
    
    /**
     * Get a location by its ID.
     * @param id The ID of the location to retrieve.
     * @return The location with the specified ID, or null if not found.
     */
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getLocationById(id: Long): Location?
    
    /**
     * Insert a new location into the database.
     * @param location The location to insert.
     * @return The ID of the newly inserted location.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long
    
    /**
     * Insert multiple locations into the database.
     * @param locations The list of locations to insert.
     * @return The list of IDs of the newly inserted locations.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocations(locations: List<Location>): List<Long>
    
    /**
     * Update an existing location in the database.
     * @param location The location to update.
     */
    @Update
    suspend fun updateLocation(location: Location)
    
    /**
     * Delete a location from the database.
     * @param location The location to delete.
     */
    @Delete
    suspend fun deleteLocation(location: Location)
    
    /**
     * Delete a location by its ID.
     * @param id The ID of the location to delete.
     */
    @Query("DELETE FROM locations WHERE id = :id")
    suspend fun deleteLocationById(id: Long)
}