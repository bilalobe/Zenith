package com.zenithtasks.data.local.db

import androidx.room.TypeConverter
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Type converters for Room database to handle date and time conversions.
 */
class DateConverters {
    /**
     * Convert from timestamp to LocalDateTime
     */
    @TypeConverter
    fun fromTimestamp(value: Long?): LocalDateTime? {
        return value?.let {
            LocalDateTime.ofInstant(Instant.ofEpochMilli(it), ZoneId.systemDefault())
        }
    }

    /**
     * Convert from LocalDateTime to timestamp
     */
    @TypeConverter
    fun toTimestamp(dateTime: LocalDateTime?): Long? {
        return dateTime?.atZone(ZoneId.systemDefault())?.toInstant()?.toEpochMilli()
    }

    /**
     * Convert from Date to LocalDateTime
     */
    @TypeConverter
    fun fromDate(date: Date?): LocalDateTime? {
        return date?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDateTime()
    }

    /**
     * Convert from LocalDateTime to Date
     */
    @TypeConverter
    fun toDate(dateTime: LocalDateTime?): Date? {
        return dateTime?.let {
            Date.from(it.atZone(ZoneId.systemDefault()).toInstant())
        }
    }
}