package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.security.authz.Unauthenticated
import misk.web.Get
import misk.web.Post
import misk.web.RequestBody
import misk.web.RequestContentType
import misk.web.Response
import misk.web.ResponseBody
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import misk.web.toResponseBody
import okhttp3.Headers.Companion.headersOf
import xyz.block.buildersyndicate.adapters.misk.auth.SessionManager
import xyz.block.buildersyndicate.core.users.User
import xyz.block.buildersyndicate.core.users.UserRepository
import java.net.URLDecoder

@Singleton
public class LoginAction @Inject constructor(
    private val userRepository: UserRepository,
    private val sessionManager: SessionManager
) : WebAction {

    data class TestUser(val externalId: String, val displayName: String, val email: String)

    private val testUsers = listOf(
        TestUser("dev-alice", "Alice", "alice@example.com"),
        TestUser("dev-bob", "Bob", "bob@example.com"),
        TestUser("dev-charlie", "Charlie", "charlie@example.com")
    )

    @Get("/login")
    @Unauthenticated
    @ResponseContentType(MediaTypes.TEXT_HTML)
    fun showLoginPage(): Response<ResponseBody> {
        val userButtons = testUsers.joinToString("\n") { user ->
            """<button type="submit" name="username" value="${user.externalId}">${user.displayName}</button>"""
        }

        val html = """
            <!DOCTYPE html>
            <html>
            <head>
                <title>Login - Builder Syndicate</title>
                <style>
                    body {
                        font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                        min-height: 100vh;
                        margin: 0;
                        background: #f5f5f5;
                    }
                    .login-container {
                        background: white;
                        padding: 2rem;
                        border-radius: 8px;
                        box-shadow: 0 2px 10px rgba(0,0,0,0.1);
                        text-align: center;
                    }
                    h1 { margin-bottom: 1.5rem; color: #333; }
                    p { color: #666; margin-bottom: 1.5rem; }
                    form { display: flex; flex-direction: column; gap: 0.75rem; }
                    button {
                        padding: 0.75rem 1.5rem;
                        font-size: 1rem;
                        border: 1px solid #ddd;
                        border-radius: 4px;
                        background: white;
                        cursor: pointer;
                        transition: background 0.2s;
                    }
                    button:hover { background: #f0f0f0; }
                </style>
            </head>
            <body>
                <div class="login-container">
                    <h1>Builder Syndicate</h1>
                    <p>Select a user to log in (dev mode)</p>
                    <form method="post" action="/login">
                        $userButtons
                    </form>
                </div>
            </body>
            </html>
        """.trimIndent()

        return Response(
            body = html.toResponseBody(),
            headers = headersOf("Content-Type", MediaTypes.TEXT_HTML)
        )
    }

    @Post("/login")
    @Unauthenticated
    @RequestContentType(MediaTypes.APPLICATION_FORM_URLENCODED)
    @ResponseContentType(MediaTypes.TEXT_HTML)
    fun handleLogin(@RequestBody body: String): Response<ResponseBody> {
        val params = body.split("&").associate { 
            val (key, value) = it.split("=", limit = 2)
            URLDecoder.decode(key, "UTF-8") to URLDecoder.decode(value, "UTF-8")
        }
        val username = params["username"] ?: return Response(
            body = "Missing username".toResponseBody(),
            statusCode = 400
        )

        val testUser = testUsers.find { it.externalId == username }
            ?: return Response(
                body = "Invalid user".toResponseBody(),
                statusCode = 400
            )

        val user = userRepository.findByExternalId(testUser.externalId)
            ?: userRepository.create(
                User(
                    externalId = testUser.externalId,
                    email = testUser.email,
                    displayName = testUser.displayName
                )
            )

        val sessionToken = sessionManager.createSession(user.id!!)

        return Response(
            body = "".toResponseBody(),
            statusCode = 302,
            headers = headersOf(
                "Location", "/",
                "Set-Cookie", "${SessionManager.COOKIE_NAME}=$sessionToken; Path=/; HttpOnly; SameSite=Lax"
            )
        )
    }
}
