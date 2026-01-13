package xyz.block.buildersyndicate.adapters.misk.auth

import misk.scope.ActionScoped
import misk.web.FakeHttpCall
import misk.web.HttpCall
import okhttp3.Headers
import org.junit.jupiter.api.Test
import xyz.block.buildersyndicate.adapters.misk.headersOf
import xyz.block.buildersyndicate.core.users.FakeUserRepository
import xyz.block.buildersyndicate.core.users.User
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class CurrentUserProviderTest {

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
  fun `returns null user when no cookie header`() {
    val provider = CurrentUserProvider(
      httpCallWithCookies(null),
      SessionManager(),
      FakeUserRepository(),
    )

    val result = provider.get()

    assertNull(result.user)
  }

  @Test
  fun `returns null user when no session cookie`() {
    val provider = CurrentUserProvider(
      httpCallWithCookies("other_cookie=value"),
      SessionManager(),
      FakeUserRepository(),
    )

    val result = provider.get()

    assertNull(result.user)
  }

  @Test
  fun `returns null user when session token invalid`() {
    val provider = CurrentUserProvider(
      httpCallWithCookies("${SessionManager.COOKIE_NAME}=invalid-token"),
      SessionManager(),
      FakeUserRepository(),
    )

    val result = provider.get()

    assertNull(result.user)
  }

  @Test
  fun `returns null user when user not found in repository`() {
    val sessionManager = SessionManager()
    val token = sessionManager.createSession(42L)

    val provider = CurrentUserProvider(
      httpCallWithCookies("${SessionManager.COOKIE_NAME}=$token"),
      sessionManager,
      FakeUserRepository(),
    )

    val result = provider.get()

    assertNull(result.user)
  }

  @Test
  fun `returns user when session is valid`() {
    val sessionManager = SessionManager()
    val token = sessionManager.createSession(42L)
    val user = User(
      id = 42L,
      externalId = "dev-alice",
      email = "alice@example.com",
      displayName = "Alice",
    )
    val userRepository = FakeUserRepository().apply { addUser(user) }

    val provider = CurrentUserProvider(
      httpCallWithCookies("${SessionManager.COOKIE_NAME}=$token"),
      sessionManager,
      userRepository,
    )

    val result = provider.get()

    assertNotNull(result.user)
    assertEquals(42L, result.user?.id)
    assertEquals("dev-alice", result.user?.externalId)
  }

  @Test
  fun `parses session cookie from multiple cookies`() {
    val sessionManager = SessionManager()
    val token = sessionManager.createSession(1L)
    val user = User(id = 1L, externalId = "test", email = "test@example.com", displayName = "Test")
    val userRepository = FakeUserRepository().apply { addUser(user) }

    val provider = CurrentUserProvider(
      httpCallWithCookies("other=foo; ${SessionManager.COOKIE_NAME}=$token; another=bar"),
      sessionManager,
      userRepository,
    )

    val result = provider.get()

    assertNotNull(result.user)
    assertEquals(1L, result.user?.id)
  }
}
