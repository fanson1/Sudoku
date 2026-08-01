package com.finley.android.sudoku

import com.finley.android.sudoku.database.DatabaseFactory
import com.finley.android.sudoku.database.SudokuRepository
import com.finley.android.sudoku.model.*
import com.finley.android.sudoku.security.JwtConfig
import com.finley.android.sudoku.security.PasswordHasher
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.application.*
import io.ktor.server.auth.*
import io.ktor.server.auth.jwt.*
import io.ktor.server.engine.*
import io.ktor.server.netty.*
import io.ktor.server.plugins.contentnegotiation.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.serialization.Serializable

@Serializable
data class UpdateProfileRequest(val username: String)

@Serializable
data class ChangePasswordRequest(val oldPassword: String, val newPassword: String)

@Serializable
data class UpdateLevelRequest(val level: Int)

fun main() {
    DatabaseFactory.init()
    embeddedServer(Netty, port = 8080, host = "0.0.0.0", module = Application::module)
        .start(wait = true)
}

fun Application.module() {
    install(ContentNegotiation) {
        json(kotlinx.serialization.json.Json {
            prettyPrint = true
            isLenient = true
            ignoreUnknownKeys = true
        })
    }

    install(Authentication) {
        jwt("auth-jwt") {
            verifier(JwtConfig.verifier)
            validate { credential ->
                if (credential.payload.getClaim("username").asString() != "") {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
        }
    }

    val repository = SudokuRepository()

    routing {
        get("/") {
            call.respondText("Sudoku Server with JWT & BCrypt is running!")
        }

        post("/register") {
            val request = call.receive<AuthRequest>()
            val existingUser = repository.getUserByUsername(request.username)
            if (existingUser != null) {
                call.respond(AuthResponse(false, message = "User already exists"))
            } else {
                val user = repository.registerUser(request)
                if (user != null) {
                    val token = JwtConfig.generateToken(user.username)
                    call.respond(AuthResponse(true, user.copy(token = token)))
                } else {
                    call.respond(AuthResponse(false, message = "Registration failed"))
                }
            }
        }

        post("/login") {
            val request = call.receive<AuthRequest>()
            val userInDb = repository.getUserByUsername(request.username)
            val hashedPassword = repository.findUser(request.username)

            if (userInDb != null && hashedPassword != null && PasswordHasher.check(request.password, hashedPassword)) {
                val token = JwtConfig.generateToken(userInDb.username)
                call.respond(AuthResponse(true, userInDb.copy(token = token)))
            } else {
                call.respond(AuthResponse(false, message = "用户名或密码错误"))
            }
        }

        get("/leaderboard") {
            val scores = repository.getLeaderboard()
            call.respond(LeaderboardResponse(scores))
        }

        get("/users") {
            val users = repository.getAllUsers()
            call.respond(users)
        }

        get("/scores") {
            val scores = repository.getLeaderboard()
            call.respond(scores)
        }

        get("/game-config/{level}") {
            val level = call.parameters["level"]?.toIntOrNull() ?: 1
            call.respond(mapOf(
                "initialHints" to GameRules.getInitialHintsForLevel(level),
                "maxMistakes" to 5
            ))
        }

        authenticate("auth-jwt") {
            get("/profile") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val user = repository.getUserByUsername(username)
                if (user != null) {
                    call.respond(user)
                } else {
                    call.respond(HttpStatusCode.NotFound)
                }
            }

            post("/update-profile") {
                val principal = call.principal<JWTPrincipal>()
                val oldUsername = principal!!.payload.getClaim("username").asString()
                val request = call.receive<UpdateProfileRequest>()
                val newUsername = request.username
                
                if (repository.getUserByUsername(newUsername) != null) {
                    return@post call.respond(HttpStatusCode.Conflict, mapOf("message" to "Username already taken"))
                }

                if (repository.updateUsername(oldUsername, newUsername)) {
                    call.respond(mapOf("success" to true))
                } else {
                    call.respond(HttpStatusCode.InternalServerError)
                }
            }

            post("/change-password") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val request = call.receive<ChangePasswordRequest>()
                val oldPassword = request.oldPassword
                val newPassword = request.newPassword

                val currentHashed = repository.findUser(username)
                if (currentHashed != null && PasswordHasher.check(oldPassword, currentHashed)) {
                    repository.updatePassword(username, PasswordHasher.hash(newPassword))
                    call.respond(mapOf("success" to true))
                } else {
                    call.respond(HttpStatusCode.Unauthorized, mapOf("message" to "Invalid old password"))
                }
            }

            post("/submit-score") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val entry = call.receive<LeaderboardEntry>()
                
                if (entry.username == username) {
                    repository.addScore(entry)
                    // 同时更新用户的最大通关关卡：完成第 N 关，则解锁到第 N + 1 关
                    repository.updateUnlockedLevel(username, entry.level + 1)
                    call.respond(mapOf("success" to true))
                } else {
                    call.respond(AuthResponse(false, message = "Forbidden: Cannot submit score for another user"))
                }
            }

            post("/update-level") {
                val principal = call.principal<JWTPrincipal>()
                val username = principal!!.payload.getClaim("username").asString()
                val request = call.receive<UpdateLevelRequest>()
                val level = request.level
                
                repository.updateUnlockedLevel(username, level)
                call.respond(mapOf("success" to true))
            }
        }
    }
}
