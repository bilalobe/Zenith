package com.zenithtasks.data.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * A data class that contains a task and its associated location (if any).
 * This is used to retrieve a task along with its location in a single query.
 */
data class TaskWithLocation(
    @Embedded val task: Task,
    
    @Relation(
        parentColumn = "locationId",
        entityColumn = "id"
    )
    val location: Location?
)