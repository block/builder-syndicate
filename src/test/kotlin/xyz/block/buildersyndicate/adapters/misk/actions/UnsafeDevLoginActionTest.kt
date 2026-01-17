package xyz.block.buildersyndicate.adapters.misk.actions

import org.junit.jupiter.api.Test
import retrofit2.http.HTTP
import xyz.block.buildersyndicate.adapters.misk.auth.SessionManager
import xyz.block.buildersyndicate.core.users.FakeUserRepository
import xyz.block.buildersyndicate.core.users.User
import java.net.HttpURLConnection.HTTP_BAD_REQUEST
import java.net.HttpURLConnection.HTTP_OK
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class UnsafeDevLoginActionTest {

  @Test
  fun `listDevUsers returns available dev users`() {
    val action = UnsafeDevLoginAction(FakeUserRepository(), SessionManager())

    val response = action.listDevUsers()

    assertEquals(3, response.users.size)
    assertTrue(response.users.any { it.username == "dev-alice" })
    assertTrue(response.users.any { it.username == "dev-bob" })
    assertTrue(response.users.any { it.username == "dev-charlie" })
  }

  @Test
  fun `login returns 400 for invalid username`() {
    val action = UnsafeDevLoginAction(FakeUserRepository(), SessionManager())

    val response = action.login(UnsafeDevLoginAction.LoginRequest("invalid-user"))

    assertEquals(HTTP_BAD_REQUEST, response.statusCode)
  }

  @Test
  fun `login creates user on first login`() {
    val userRepository = FakeUserRepository()
    val action = UnsafeDevLoginAction(userRepository, SessionManager())

    val response = action.login(UnsafeDevLoginAction.LoginRequest("dev-alice"))

    assertEquals(HTTP_OK, response.statusCode)
    assertEquals(1, userRepository.createdUsers.size)
    assertEquals("dev-alice", userRepository.createdUsers[0].externalId)
  }

  @Test
  fun `login uses existing user if found`() {
    val existingUser = User(id = 42L, externalId = "dev-bob", email = "bob@example.com", displayName = "Bob")
    val userRepository = FakeUserRepository().apply { addUser(existingUser) }
    val action = UnsafeDevLoginAction(userRepository, SessionManager())

    val response = action.login(UnsafeDevLoginAction.LoginRequest("dev-bob"))

    assertEquals(HTTP_OK, response.statusCode)
    assertEquals(0, userRepository.createdUsers.size)
  }

  @Test
  fun `login returns session token`() {
    val existingUser = User(id = 1L, externalId = "dev-alice", email = "alice@example.com", displayName = "Alice")
    val userRepository = FakeUserRepository().apply { addUser(existingUser) }
    val action = UnsafeDevLoginAction(userRepository, SessionManager())

    val response = action.login(UnsafeDevLoginAction.LoginRequest("dev-alice"))

    assertEquals(HTTP_OK, response.statusCode)
    assertNotNull(response.body.sessionToken)
    assertTrue(response.body.sessionToken.isNotEmpty())
  }

  @Test
  fun `login sets session cookie`() {
    val existingUser = User(id = 1L, externalId = "dev-alice", email = "alice@example.com", displayName = "Alice")
    val userRepository = FakeUserRepository().apply { addUser(existingUser) }
    val action = UnsafeDevLoginAction(userRepository, SessionManager())

    val response = action.login(UnsafeDevLoginAction.LoginRequest("dev-alice"))

    val setCookie = response.headers[SessionManager.SET_COOKIE_HEADER]
    assertTrue(setCookie?.startsWith("${SessionManager.COOKIE_NAME}=") == true)
    assertTrue(setCookie?.contains("HttpOnly") == true)
  }
}
