package com.zenithtasks.data.local.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.zenithtasks.data.model.Location
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for Location entities.
 * Provides methods to interact with the locations table.
 */
@Dao
interface LocationDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLocation(location: Location): Long

    @Update
    suspend fun updateLocation(location: Location)

    @Delete
    suspend fun deleteLocation(location: Location)

    @Query("SELECT * FROM locations WHERE id = :locationId")
    suspend fun getLocationById(locationId: Long): Location?

    @Query("SELECT * FROM locations ORDER BY name ASC")
    fun getAllLocations(): Flow<List<Location>>

    /**
     * Get all locations as a regular list (non-Flow version)
     */
    @Query("SELECT * FROM locations ORDER BY name ASC")
    suspend fun getAllLocationsList(): List<Location>
}