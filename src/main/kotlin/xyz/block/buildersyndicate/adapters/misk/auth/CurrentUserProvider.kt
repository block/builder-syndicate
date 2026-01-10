package xyz.block.buildersyndicate.adapters.misk.auth

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.scope.ActionScoped
import misk.scope.ActionScopedProvider
import misk.web.HttpCall
import xyz.block.buildersyndicate.core.users.UserRepository

@Singleton
class CurrentUserProvider @Inject constructor(
    private val httpCall: ActionScoped<HttpCall>,
    private val sessionManager: SessionManager,
    private val userRepository: UserRepository
) : ActionScopedProvider<CurrentUser> {

    override fun get(): CurrentUser {
        val call = httpCall.get()
        val cookies = call.requestHeaders[SessionManager.COOKIE_HEADER] ?: return CurrentUser(null)

        val sessionToken = cookies.split(";")
            .map { it.trim() }
            .find { it.startsWith("${SessionManager.COOKIE_NAME}=") }
            ?.substringAfter("=")
            ?: return CurrentUser(null)

        val userId = sessionManager.getUserId(sessionToken) ?: return CurrentUser(null)
        return CurrentUser(userRepository.findById(userId))
    }
}
