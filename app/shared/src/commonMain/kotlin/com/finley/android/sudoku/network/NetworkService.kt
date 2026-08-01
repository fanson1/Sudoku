package com.finley.android.sudoku.network

import com.finley.android.sudoku.getBaseUrl
import com.finley.android.sudoku.model.*
import com.finley.android.sudoku.util.Persistence
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

object NetworkService {
    private val client = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                prettyPrint = true
                isLenient = true
            })
        }
    }

    private var authToken: String? = null
    var currentUser: User? = null

    fun setAuthToken(token: String?) {
        this.authToken = token
    }

    suspend fun login(request: AuthRequest): AuthResponse {
        return try {
            val response: AuthResponse = client.post("${getBaseUrl()}/login") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
            if (response.success && response.user != null) {
                currentUser = response.user
                authToken = response.user?.token
                Persistence.saveAuthToken(authToken)
                Persistence.saveUsername(currentUser?.username)
            }
            response
        } catch (e: Exception) {
            AuthResponse(false, message = e.message ?: "Network error")
        }
    }

    suspend fun register(request: AuthRequest): AuthResponse {
        return try {
            val response: AuthResponse = client.post("${getBaseUrl()}/register") {
                setBody(request)
                contentType(ContentType.Application.Json)
            }.body()
            if (response.success && response.user != null) {
                currentUser = response.user
                authToken = response.user?.token
                Persistence.saveAuthToken(authToken)
                Persistence.saveUsername(currentUser?.username)
            }
            response
        } catch (e: Exception) {
            AuthResponse(false, message = e.message ?: "Network error")
        }
    }

    fun logout() {
        currentUser = null
        authToken = null
        Persistence.clearAuth()
    }

    suspend fun getUserProfile(): User? {
        val token = authToken ?: Persistence.getAuthToken() ?: return null
        authToken = token
        return try {
            val user: User = client.get("${getBaseUrl()}/profile") {
                header(HttpHeaders.Authorization, "Bearer $token")
            }.body()
            currentUser = user
            Persistence.saveUsername(user.username)
            user
        } catch (e: Exception) {
            null
        }
    }

    suspend fun updateProfile(username: String): Boolean {
        val token = authToken ?: return false
        return try {
            val response = client.post("${getBaseUrl()}/update-profile") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(mapOf("username" to username))
                contentType(ContentType.Application.Json)
            }
            if (response.status.isSuccess()) {
                currentUser = currentUser?.copy(username = username)
                Persistence.saveUsername(username)
                true
            } else false
        } catch (e: Exception) {
            false
        }
    }

    suspend fun changePassword(old: String, new: String): Boolean {
        val token = authToken ?: return false
        return try {
            client.post("${getBaseUrl()}/change-password") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(mapOf("oldPassword" to old, "newPassword" to new))
                contentType(ContentType.Application.Json)
            }.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun getLeaderboard(): LeaderboardResponse {
        return try {
            client.get("${getBaseUrl()}/leaderboard").body()
        } catch (e: Exception) {
            LeaderboardResponse(emptyList())
        }
    }

    suspend fun updateUnlockedLevel(level: Int): Boolean {
        val token = authToken ?: return false
        return try {
            client.post("${getBaseUrl()}/update-level") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(mapOf("level" to level))
                contentType(ContentType.Application.Json)
            }.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    suspend fun submitScore(level: Int, score: Int): Boolean {
        val user = currentUser ?: getUserProfile() ?: return false
        val token = authToken ?: Persistence.getAuthToken() ?: return false
        return try {
            val entry = LeaderboardEntry(rank = 0, username = user.username, score = score, level = level)
            val response = client.post("${getBaseUrl()}/submit-score") {
                header(HttpHeaders.Authorization, "Bearer $token")
                setBody(entry)
                contentType(ContentType.Application.Json)
            }
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }
}
