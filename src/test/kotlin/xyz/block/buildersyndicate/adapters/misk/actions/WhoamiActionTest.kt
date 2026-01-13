package xyz.block.buildersyndicate.adapters.misk.actions

import misk.scope.ActionScoped
import org.junit.jupiter.api.Test
import xyz.block.buildersyndicate.adapters.misk.auth.CurrentUser
import xyz.block.buildersyndicate.core.users.User
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class WhoamiActionTest {

  private fun actionScopedOf(currentUser: CurrentUser): ActionScoped<CurrentUser> {
    return object : ActionScoped<CurrentUser> {
      override fun get(): CurrentUser = currentUser
    }
  }

  @Test
  fun `returns 401 when no user in session`() {
    val action = WhoamiAction(actionScopedOf(CurrentUser(null)))

    val response = action.whoami()

    assertEquals(401, response.statusCode)
  }

  @Test
  fun `returns user data when authenticated`() {
    val user = User(
      id = 42L,
      externalId = "dev-alice",
      email = "alice@example.com",
      displayName = "Alice",
      avatarUrl = "https://example.com/alice.png",
      createdAt = Instant.now(),
      updatedAt = Instant.now(),
    )
    val action = WhoamiAction(actionScopedOf(CurrentUser(user)))

    val response = action.whoami()

    assertEquals(200, response.statusCode)
    assertNotNull(response.body)
    assertEquals(42L, response.body.id)
    assertEquals("dev-alice", response.body.externalId)
    assertEquals("alice@example.com", response.body.email)
    assertEquals("Alice", response.body.displayName)
    assertEquals("https://example.com/alice.png", response.body.avatarUrl)
  }

  @Test
  fun `returns null avatarUrl when user has none`() {
    val user = User(
      id = 1L,
      externalId = "dev-bob",
      email = "bob@example.com",
      displayName = "Bob",
      avatarUrl = null,
    )
    val action = WhoamiAction(actionScopedOf(CurrentUser(user)))

    val response = action.whoami()

    assertEquals(200, response.statusCode)
    assertEquals(null, response.body.avatarUrl)
  }
}
