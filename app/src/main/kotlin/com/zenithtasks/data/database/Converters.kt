package com.zenithtasks.data.database

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters for Room database.
 * These converters allow Room to store complex data types like Date.
 */
class Converters {
    /**
     * Converts a timestamp to a Date object.
     *
     * @param value The timestamp in milliseconds
     * @return The Date object, or null if the timestamp is null
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts a Date object to a timestamp.
     *
     * @param date The Date object
     * @return The timestamp in milliseconds, or null if the Date is null
     */
    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    /**
     * Converts a comma-separated string to a list of strings.
     *
     * @param value The comma-separated string
     * @return The list of strings, or an empty list if the input is null or empty
     */
    @TypeConverter
    fun fromString(value: String?): List<String> {
        return value?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    /**
     * Converts a list of strings to a comma-separated string.
     *
     * @param list The list of strings
     * @return The comma-separated string, or null if the list is null
     */
    @TypeConverter
    fun fromList(list: List<String>?): String? {
        return list?.joinToString(",")
    }
}