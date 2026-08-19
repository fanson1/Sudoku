package com.finley.android.sudoku.util

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class DailyUtilTest {

    @Test
    fun dailySeedIsDeterministic() {
        assertEquals(DailyUtil.dailySeed("2026-08-14"), DailyUtil.dailySeed("2026-08-14"))
        assertTrue(DailyUtil.dailySeed("2026-08-15") > DailyUtil.dailySeed("2026-08-14"))
    }

    @Test
    fun isConsecutiveDetectsAdjacentDays() {
        assertTrue(DailyUtil.isConsecutive("2026-08-14", "2026-08-15"))
        assertFalse(DailyUtil.isConsecutive("2026-08-14", "2026-08-16"))
        assertFalse(DailyUtil.isConsecutive(null, "2026-08-15"))
        assertFalse(DailyUtil.isConsecutive("2026-08-14", "not-a-date"))
    }
}
