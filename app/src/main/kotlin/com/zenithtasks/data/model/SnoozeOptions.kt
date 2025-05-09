package com.zenithtasks.data.model

import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

/**
 * Enum representing standard options for snoozing tasks
 */
enum class SnoozeOptions(val displayName: String, val duration: Duration) {
    ONE_HOUR("1 hour", Duration.ofHours(1)),
    THREE_HOURS("3 hours", Duration.ofHours(3)),
    UNTIL_TOMORROW("Until tomorrow", Duration.ofHours(24)),
    UNTIL_NEXT_WEEK("Until next week", Duration.ofDays(7)),
    CUSTOM("Custom...", Duration.ZERO); // Special value for custom duration
    
    /**
     * Calculate the date when a task should reappear after being snoozed with this option
     * @return Date when the task should reappear
     */
    fun getSnoozeEndDate(): Date {
        val now = LocalDateTime.now()
        val snoozeEnd = now.plus(duration)
        return Date.from(snoozeEnd.atZone(ZoneId.systemDefault()).toInstant())
    }
    
    /**
     * For the UNTIL_TOMORROW option, sets the time to 9:00 AM the next day
     * instead of exactly 24 hours later
     * @return Date at 9:00 AM the next day
     */
    fun getNextDayMorning(): Date {
        val tomorrow = LocalDateTime.now().plusDays(1)
        val morning = tomorrow.withHour(9).withMinute(0).withSecond(0)
        return Date.from(morning.atZone(ZoneId.systemDefault()).toInstant())
    }
    
    /**
     * Calculate the snooze end date, handling special cases
     * @return Date when the task should reappear
     */
    fun calculateSnoozeUntil(): Date {
        return when(this) {
            UNTIL_TOMORROW -> getNextDayMorning()
            CUSTOM -> throw IllegalStateException("Custom duration requires explicit date")
            else -> getSnoozeEndDate()
        }
    }
    
    companion object {
        /**
         * Create a custom snooze option with the given end date
         * @param endDate Date when the task should reappear
         * @return A pair of CUSTOM enum value and the calculated duration
         */
        fun custom(endDate: Date): Pair<SnoozeOptions, Duration> {
            val now = LocalDateTime.now()
            val end = endDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime()
            val customDuration = Duration.between(now, end)
            return Pair(CUSTOM, customDuration)
        }
    }
}