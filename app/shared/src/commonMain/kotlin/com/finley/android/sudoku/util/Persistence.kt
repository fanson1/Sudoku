package com.finley.android.sudoku.util

import com.finley.android.sudoku.model.Difficulty
import com.finley.android.sudoku.ui.theme.ThemeMode
import com.russhwolf.settings.Settings

object Persistence {
    private val settings: Settings = Settings()
    private const val KEY_UNLOCKED_LEVEL = "unlocked_level"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USERNAME = "username"
    private const val KEY_THEME_MODE = "theme_mode"
    private const val KEY_DAILY_COMPLETED = "daily_completed"
    private const val KEY_DAILY_CHAIN = "daily_chain"
    private const val KEY_DAILY_LAST = "daily_last"
    private const val KEY_AUTO_ERASE_NOTES = "auto_erase_notes"
    private const val KEY_SHOW_CONFLICTS = "show_conflicts"
    private const val KEY_GAMES_PLAYED = "games_played"
    private const val KEY_GAMES_WON = "games_won"
    private const val KEY_BEST_SCORE = "best_score"
    private const val KEY_BEST_SCORES = "best_scores"

    fun getDailyCompletedDate(): String? = settings.getStringOrNull(KEY_DAILY_COMPLETED)

    fun isDailyCompleted(dateKey: String): Boolean = getDailyCompletedDate() == dateKey

    fun saveDailyCompleted(dateKey: String) {
        val today = DailyUtil.todayDateKey()
        val last = settings.getStringOrNull(KEY_DAILY_LAST)
        val chain = settings.getInt(KEY_DAILY_CHAIN, 0)
        val newChain = if (DailyUtil.isConsecutive(last, today)) chain + 1 else 1
        settings.putString(KEY_DAILY_COMPLETED, dateKey)
        settings.putString(KEY_DAILY_LAST, today)
        settings.putInt(KEY_DAILY_CHAIN, newChain)
    }

    fun getDailyChain(): Int = settings.getInt(KEY_DAILY_CHAIN, 0)

    fun getThemeMode(): ThemeMode {
        return when (settings.getStringOrNull(KEY_THEME_MODE)) {
            "light" -> ThemeMode.LIGHT
            "dark" -> ThemeMode.DARK
            else -> ThemeMode.SYSTEM
        }
    }

    fun saveThemeMode(mode: ThemeMode) {
        settings.putString(KEY_THEME_MODE, when (mode) {
            ThemeMode.LIGHT -> "light"
            ThemeMode.DARK -> "dark"
            ThemeMode.SYSTEM -> "system"
        })
    }

    fun getUnlockedLevel(): Int {
        return settings.getInt(KEY_UNLOCKED_LEVEL, 1)
    }

    fun saveUnlockedLevel(level: Int) {
        val current = getUnlockedLevel()
        if (level > current) {
            settings.putInt(KEY_UNLOCKED_LEVEL, level)
        }
    }

    fun saveAuthToken(token: String?) {
        if (token != null) settings.putString(KEY_AUTH_TOKEN, token)
        else settings.remove(KEY_AUTH_TOKEN)
    }

    fun getAuthToken(): String? = settings.getStringOrNull(KEY_AUTH_TOKEN)

    fun saveUsername(username: String?) {
        if (username != null) settings.putString(KEY_USERNAME, username)
        else settings.remove(KEY_USERNAME)
    }

    fun getUsername(): String? = settings.getStringOrNull(KEY_USERNAME)

    fun clearAuth() {
        settings.remove(KEY_AUTH_TOKEN)
        settings.remove(KEY_USERNAME)
        settings.remove(KEY_UNLOCKED_LEVEL)
    }

    // Game settings
    fun getAutoEraseNotes(): Boolean = settings.getBoolean(KEY_AUTO_ERASE_NOTES, true)
    fun saveAutoEraseNotes(value: Boolean) { settings.putBoolean(KEY_AUTO_ERASE_NOTES, value) }

    fun getShowConflicts(): Boolean = settings.getBoolean(KEY_SHOW_CONFLICTS, true)
    fun saveShowConflicts(value: Boolean) { settings.putBoolean(KEY_SHOW_CONFLICTS, value) }

    // Statistics
    fun getGamesPlayed(): Int = settings.getInt(KEY_GAMES_PLAYED, 0)
    fun getGamesWon(): Int = settings.getInt(KEY_GAMES_WON, 0)
    fun getBestScore(): Int = settings.getInt(KEY_BEST_SCORE, 0)

    fun recordGame(difficulty: Difficulty, won: Boolean, score: Int) {
        settings.putInt(KEY_GAMES_PLAYED, getGamesPlayed() + 1)
        if (won) settings.putInt(KEY_GAMES_WON, getGamesWon() + 1)
        if (score > getBestScore()) settings.putInt(KEY_BEST_SCORE, score)

        val key = bestScoreKey(difficulty)
        val perDiff = settings.getInt(key, 0)
        if (score > perDiff) settings.putInt(key, score)
    }

    fun getBestScoreFor(difficulty: Difficulty): Int =
        settings.getInt(bestScoreKey(difficulty), 0)

    private fun bestScoreKey(difficulty: Difficulty): String =
        "$KEY_BEST_SCORES:${difficulty.name}"
}
