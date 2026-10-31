package com.finley.android.sudoku.util

/** Boundaries between the free experience and the one-time Pro unlock. */
object FreeTier {
    const val TOTAL_LEVELS = 50
    const val MAX_FREE_LEVEL = 10

    fun isLevelFree(level: Int): Boolean = level <= MAX_FREE_LEVEL

    fun isLevelAvailable(level: Int, isPro: Boolean): Boolean =
        level <= TOTAL_LEVELS && (isLevelFree(level) || isPro)
}
