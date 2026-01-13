package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.scope.ActionScoped
import misk.security.authz.Unauthenticated
import misk.web.HttpCall
import misk.web.Post
import misk.web.Response
import misk.web.ResponseBody
import misk.web.actions.WebAction
import misk.web.toResponseBody
import xyz.block.buildersyndicate.adapters.misk.auth.SessionManager
import xyz.block.buildersyndicate.adapters.misk.headersOf

@Singleton
public class LogoutAction @Inject constructor(
  private val sessionManager: SessionManager,
  private val httpCall: ActionScoped<HttpCall>,
) : WebAction {

  @Post("/logout")
  @Unauthenticated
  fun handleLogout(): Response<ResponseBody> {
    val cookies = httpCall.get().requestHeaders["Cookie"]
    if (cookies != null) {
      val sessionToken = cookies.split(";")
        .map { it.trim() }
        .find { it.startsWith("${SessionManager.COOKIE_NAME}=") }
        ?.substringAfter("=")

      if (sessionToken != null) {
        sessionManager.invalidateSession(sessionToken)
      }
    }

    return Response(
      body = "".toResponseBody(),
      statusCode = 302,
      headers = headersOf(
        "Location" to "/",
        "Set-Cookie" to "${SessionManager.COOKIE_NAME}=; Path=/; HttpOnly; Max-Age=0",
      ),
    )
  }
}
