package com.finley.android.sudoku.util

import kotlinx.datetime.Clock
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * Deterministic "puzzle of the day" helpers. The same date always maps to the
 * same puzzle on every device, so all players face the same board each day.
 */
object DailyUtil {
    const val DATE_UTC = "UTC"

    /** Today's date as "yyyy-MM-dd" in UTC. */
    fun todayDateKey(): String =
        Clock.System.now().toLocalDateTime(TimeZone.UTC).date.toString()

    /**
     * A stable, monotonic seed derived from the date key so the generator
     * reproduces the exact same puzzle for the given day.
     */
    fun dailySeed(dateKey: String): Long {
        return LocalDate.parse(dateKey).toEpochDays().toLong()
    }

    /** Day number so App.kt can show "DAY 1234" styling. */
    fun dayNumber(dateKey: String): Long {
        return LocalDate.parse(dateKey).toEpochDays().toLong()
    }

    /** True if `next` is exactly the day after `prev` (null prev → false). */
    fun isConsecutive(prev: String?, next: String): Boolean {
        if (prev == null) return false
        return try {
            LocalDate.parse(next).toEpochDays() - LocalDate.parse(prev).toEpochDays() == 1
        } catch (_: Exception) {
            false
        }
    }
}