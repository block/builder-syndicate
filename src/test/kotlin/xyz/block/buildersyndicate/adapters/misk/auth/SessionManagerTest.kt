package xyz.block.buildersyndicate.adapters.misk.auth

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class SessionManagerTest {

    @Test
    fun `createSession returns unique tokens`() {
        val sessionManager = SessionManager()

        val token1 = sessionManager.createSession(1L)
        val token2 = sessionManager.createSession(2L)

        assertNotEquals(token1, token2)
    }

    @Test
    fun `createSession tokens are URL-safe base64`() {
        val sessionManager = SessionManager()

        val token = sessionManager.createSession(1L)

        assertNotNull(token)
        assert(token.matches(Regex("^[A-Za-z0-9_-]+$"))) { "Token should be URL-safe base64" }
    }

    @Test
    fun `getUserId returns userId for valid session`() {
        val sessionManager = SessionManager()
        val token = sessionManager.createSession(42L)

        val userId = sessionManager.getUserId(token)

        assertEquals(42L, userId)
    }

    @Test
    fun `getUserId returns null for unknown token`() {
        val sessionManager = SessionManager()

        val userId = sessionManager.getUserId("unknown-token")

        assertNull(userId)
    }

    @Test
    fun `invalidateSession removes session`() {
        val sessionManager = SessionManager()
        val token = sessionManager.createSession(1L)

        sessionManager.invalidateSession(token)

        assertNull(sessionManager.getUserId(token))
    }

    @Test
    fun `invalidateSession is idempotent`() {
        val sessionManager = SessionManager()
        val token = sessionManager.createSession(1L)

        sessionManager.invalidateSession(token)
        sessionManager.invalidateSession(token)

        assertNull(sessionManager.getUserId(token))
    }

    @Test
    fun `multiple sessions for same user are independent`() {
        val sessionManager = SessionManager()

        val token1 = sessionManager.createSession(1L)
        val token2 = sessionManager.createSession(1L)

        sessionManager.invalidateSession(token1)

        assertNull(sessionManager.getUserId(token1))
        assertEquals(1L, sessionManager.getUserId(token2))
    }
}
