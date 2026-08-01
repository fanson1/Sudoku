package com.finley.android.sudoku.security

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.*

object JwtConfig {
    private const val SECRET = "sudoku-secret-key-2024" // In production, use environment variable
    private const val ISSUER = "com.finley.sudoku"
    private const val VALIDITY_MS = 36_000_000 * 24 // 24 hours
    private val algorithm = Algorithm.HMAC256(SECRET)

    val verifier = JWT
        .require(algorithm)
        .withIssuer(ISSUER)
        .build()

    fun generateToken(username: String): String = JWT.create()
        .withSubject("Authentication")
        .withIssuer(ISSUER)
        .withClaim("username", username)
        .withExpiresAt(Date(System.currentTimeMillis() + VALIDITY_MS))
        .sign(algorithm)
}
