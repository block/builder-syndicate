package xyz.block.buildersyndicate.adapters.misk.actions

import misk.scope.ActionScoped
import misk.web.FakeHttpCall
import misk.web.HttpCall
import okhttp3.Headers
import org.junit.jupiter.api.Test
import xyz.block.buildersyndicate.adapters.misk.auth.SessionManager
import xyz.block.buildersyndicate.adapters.misk.headersOf
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

class LogoutActionTest {

  private fun httpCallWithCookies(cookies: String? = null): ActionScoped<HttpCall> {
    val headers = if (cookies != null) {
      headersOf(SessionManager.COOKIE_HEADER to cookies)
    } else {
      Headers.EMPTY
    }
    val httpCall = FakeHttpCall(requestHeaders = headers)
    return object : ActionScoped<HttpCall> {
      override fun get(): HttpCall = httpCall
    }
  }

  @Test
  fun `handleLogout redirects to root`() {
    val action = LogoutAction(SessionManager(), httpCallWithCookies(null))

    val response = action.handleLogout()

    assertEquals(302, response.statusCode)
    assertEquals("/", response.headers["Location"])
  }

  @Test
  fun `handleLogout clears session cookie`() {
    val action = LogoutAction(SessionManager(), httpCallWithCookies(null))

    val response = action.handleLogout()

    val setCookie = response.headers[SessionManager.SET_COOKIE_HEADER]
    assertTrue(setCookie?.contains("${SessionManager.COOKIE_NAME}=") == true)
    assertTrue(setCookie?.contains("Max-Age=0") == true)
  }

  @Test
  fun `handleLogout invalidates session`() {
    val sessionManager = SessionManager()
    val token = sessionManager.createSession(1L)
    val action = LogoutAction(sessionManager, httpCallWithCookies("${SessionManager.COOKIE_NAME}=$token"))

    action.handleLogout()

    assertNull(sessionManager.getUserId(token))
  }

  @Test
  fun `handleLogout works without session cookie`() {
    val action = LogoutAction(SessionManager(), httpCallWithCookies("other=value"))

    val response = action.handleLogout()

    assertEquals(302, response.statusCode)
  }

  @Test
  fun `handleLogout works with no cookies at all`() {
    val action = LogoutAction(SessionManager(), httpCallWithCookies(null))

    val response = action.handleLogout()

    assertEquals(302, response.statusCode)
  }
}
