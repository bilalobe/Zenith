package com.zenithtasks.data.model

import androidx.room.Embedded
import androidx.room.Relation
import com.zenithtasks.data.local.entity.TaskEntity

/**
 * A data class that represents a Task with its associated Location.
 * This is used for Room's @Relation query to join tasks and locations.
 */
data class TaskWithLocation(
    @Embedded
    val task: TaskEntity,
    
    @Relation(
        parentColumn = "locationId",
        entityColumn = "id"
    )
    val location: Location?
)