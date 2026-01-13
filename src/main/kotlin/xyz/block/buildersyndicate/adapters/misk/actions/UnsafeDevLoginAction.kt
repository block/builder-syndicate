package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.security.authz.Unauthenticated
import misk.web.Get
import misk.web.Post
import misk.web.RequestBody
import misk.web.RequestContentType
import misk.web.Response
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import xyz.block.buildersyndicate.adapters.misk.auth.SessionManager
import xyz.block.buildersyndicate.adapters.misk.headersOf
import xyz.block.buildersyndicate.core.users.User
import xyz.block.buildersyndicate.core.users.UserRepository

@Singleton
class UnsafeDevLoginAction @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : WebAction {

    data class DevUser(val username: String, val displayName: String)
    data class DevUsersResponse(val users: List<DevUser>)
    data class LoginRequest(val username: String)
    data class LoginResponse(val sessionToken: String, val user: DevUser)

    private val devUsers = listOf(
        DevUser("dev-alice", "Alice"),
        DevUser("dev-bob", "Bob"),
        DevUser("dev-charlie", "Charlie")
    )

    private val devUserDetails = mapOf(
        "dev-alice" to ("alice@example.com" to "Alice"),
        "dev-bob" to ("bob@example.com" to "Bob"),
        "dev-charlie" to ("charlie@example.com" to "Charlie")
    )

    @Get("/api/v1/auth/dev-users")
    @Unauthenticated
    @ResponseContentType(MediaTypes.APPLICATION_JSON)
    fun listDevUsers(): DevUsersResponse {
        return DevUsersResponse(devUsers)
    }

    @Post("/api/v1/auth/login")
    @Unauthenticated
    @RequestContentType(MediaTypes.APPLICATION_JSON)
    @ResponseContentType(MediaTypes.APPLICATION_JSON)
    fun login(@RequestBody request: LoginRequest): Response<LoginResponse> {
        val devUser = devUsers.find { it.username == request.username }
            ?: return Response(
                body = LoginResponse("", DevUser("", "")),
                statusCode = 400
            )

        val details = devUserDetails[request.username]!!

        val user = userRepository.findByExternalId(request.username)
            ?: userRepository.create(
                User(
                    externalId = request.username,
                    email = details.first,
                    displayName = details.second
                )
            )

        val sessionToken = sessionManager.createSession(user.id!!)

        return Response(
            body = LoginResponse(sessionToken, devUser),
            statusCode = 200,
            headers = headersOf(
                SessionManager.SET_COOKIE_HEADER to "${SessionManager.COOKIE_NAME}=$sessionToken; Path=/; HttpOnly; SameSite=Lax"
            )
        )
    }
}
