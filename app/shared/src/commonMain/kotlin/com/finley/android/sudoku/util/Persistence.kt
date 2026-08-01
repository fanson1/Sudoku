package com.finley.android.sudoku.util

import com.russhwolf.settings.Settings

object Persistence {
    private val settings: Settings = Settings()
    private const val KEY_UNLOCKED_LEVEL = "unlocked_level"
    private const val KEY_AUTH_TOKEN = "auth_token"
    private const val KEY_USERNAME = "username"

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
        settings.remove(KEY_UNLOCKED_LEVEL) // 退出登录时清除本地保存的关卡进度
    }
}
