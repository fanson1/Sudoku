package com.finley.android.sudoku.model

import kotlinx.serialization.Serializable

@Serializable
data class User(
    val id: String,
    val username: String,
    val token: String? = null,
    val unlockedLevel: Int = 1
)

@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)

@Serializable
data class AuthResponse(
    val success: Boolean,
    val user: User? = null,
    val message: String? = null
)

@Serializable
data class LeaderboardEntry(
    val rank: Int,
    val username: String,
    val score: Int,
    val level: Int
)

@Serializable
data class LeaderboardResponse(
    val entries: List<LeaderboardEntry>
)
