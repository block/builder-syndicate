package xyz.block.buildersyndicate.adapters.misk.auth

import jakarta.inject.Singleton
import java.security.SecureRandom
import java.util.Base64
import java.util.concurrent.ConcurrentHashMap

@Singleton
public class SessionManager {
    private val sessions = ConcurrentHashMap<String, Long>()
    private val random = SecureRandom()

    fun createSession(userId: Long): String {
        val bytes = ByteArray(32)
        random.nextBytes(bytes)
        val token = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        sessions[token] = userId
        return token
    }

    fun getUserId(token: String): Long? = sessions[token]

    fun invalidateSession(token: String) {
        sessions.remove(token)
    }

    companion object {
        const val COOKIE_NAME = "bs_session"
    }
}
