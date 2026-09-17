package com.finley.android.sudoku

import com.finley.android.sudoku.database.DatabaseFactory
import com.finley.android.sudoku.model.AuthResponse
import com.finley.android.sudoku.model.LeaderboardResponse
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlin.test.*
import kotlinx.serialization.json.Json

class ApplicationTest {

    @BeforeTest
    fun setUp() {
        DatabaseFactory.init("jdbc:h2:mem:testDb;DB_CLOSE_DELAY=-1")
    }

    private val json = Json { ignoreUnknownKeys = true }

    private fun registerAndLogin(username: String): String {
        var token: String? = null
        testApplication {
            application { module() }
            client.post("/register") {
                contentType(ContentType.Application.Json)
                setBody("""{"username":"$username","password":"secret123"}""")
            }
            val login = client.post("/login") {
                contentType(ContentType.Application.Json)
                setBody("""{"username":"$username","password":"secret123"}""")
            }
            val auth = json.decodeFromString<AuthResponse>(login.bodyAsText())
            assertTrue(auth.success, "login should succeed")
            token = auth.user!!.token
        }
        return token!!
    }

    @Test
    fun testRoot() = testApplication {
        application { module() }
        val response = client.get("/")
        assertEquals(HttpStatusCode.OK, response.status)
        assertEquals("Sudoku Server with JWT & BCrypt is running!", response.bodyAsText())
    }

    @Test
    fun testRegisterAndLogin() = testApplication {
        application { module() }
        val username = "player_${System.nanoTime()}"

        val register = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username","password":"secret123"}""")
        }
        assertEquals(HttpStatusCode.OK, register.status)
        val registerAuth = json.decodeFromString<AuthResponse>(register.bodyAsText())
        assertTrue(registerAuth.success)
        assertNotNull(registerAuth.user)
        assertNotNull(registerAuth.user!!.token, "register should issue a token")

        val login = client.post("/login") {
            contentType(ContentType.Application.Json)
            setBody("""{"username":"$username","password":"secret123"}""")
        }
        val loginAuth = json.decodeFromString<AuthResponse>(login.bodyAsText())
        assertTrue(loginAuth.success)
    }

    @Test
    fun testDuplicateRegistrationRejected() = testApplication {
        application { module() }
        val username = "dup_${System.nanoTime()}"
        val body = ("""{"username":"$username","password":"secret123"}""")

        client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val second = client.post("/register") {
            contentType(ContentType.Application.Json)
            setBody(body)
        }
        val auth = json.decodeFromString<AuthResponse>(second.bodyAsText())
        assertFalse(auth.success, "duplicate username must be rejected")
    }

    @Test
    fun testProtectedRouteRequiresAuth() = testApplication {
        application { module() }
        val response = client.get("/profile")
        assertEquals(HttpStatusCode.Unauthorized, response.status)
    }

    @Test
    fun testSubmitScoreUpdatesLeaderboard() = testApplication {
        application { module() }
        val username = "score_${System.nanoTime()}"
        val token = registerAndLogin(username)

        val submit = client.post("/submit-score") {
            contentType(ContentType.Application.Json)
            bearerAuth(token)
            setBody("""{"rank":0,"username":"$username","score":5000,"level":1}""")
        }
        assertEquals(HttpStatusCode.OK, submit.status)

        val leaderboard = client.get("/leaderboard")
        assertEquals(HttpStatusCode.OK, leaderboard.status)
        val board = json.decodeFromString<LeaderboardResponse>(leaderboard.bodyAsText())
        assertTrue(board.entries.any { it.username == username && it.score == 5000 })
    }

    @Test
    fun testProfileReturnsOwnUser() = testApplication {
        application { module() }
        val username = "prof_${System.nanoTime()}"
        val token = registerAndLogin(username)

        val profile = client.get("/profile") {
            bearerAuth(token)
        }
        assertEquals(HttpStatusCode.OK, profile.status)
        val me = json.decodeFromString<com.finley.android.sudoku.model.User>(profile.bodyAsText())
        assertEquals(username, me.username)
    }
}