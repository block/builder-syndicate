package xyz.block.buildersyndicate.adapters.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.block.buildersyndicate.adapters.db.jooq.tables.Users.Companion.USERS
import xyz.block.buildersyndicate.core.users.User
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull

class JooqUserRepositoryTest {
  private lateinit var connection: Connection
  private lateinit var dsl: DSLContext
  private lateinit var repository: JooqUserRepository

  @BeforeEach
  fun setup() {
    connection = DriverManager.getConnection(
      "jdbc:mysql://localhost:3307/buildersyndicate",
      "root",
      "root",
    )
    dsl = DSL.using(connection, SQLDialect.MYSQL)
    repository = JooqUserRepository(dsl)
    dsl.deleteFrom(USERS).execute()
  }

  @AfterEach
  fun teardown() {
    connection.close()
  }

  @Test
  fun `create user then findById returns it with matching fields`() {
    val user = User(
      externalId = "github|123",
      email = "test@example.com",
      displayName = "Test User",
      avatarUrl = "https://example.com/avatar.png",
    )

    val created = repository.create(user)

    assertNotNull(created.id)
    assertNotNull(created.createdAt)
    assertNotNull(created.updatedAt)

    val found = repository.findById(created.id!!)
    assertNotNull(found)
    assertEquals(created.id, found.id)
    assertEquals("github|123", found.externalId)
    assertEquals("test@example.com", found.email)
    assertEquals("Test User", found.displayName)
    assertEquals("https://example.com/avatar.png", found.avatarUrl)
  }

  @Test
  fun `findByExternalId returns matching user`() {
    val user = User(
      externalId = "github|456",
      email = "external@example.com",
      displayName = "External User",
    )

    val created = repository.create(user)
    val found = repository.findByExternalId("github|456")

    assertNotNull(found)
    assertEquals(created.id, found.id)
    assertEquals("github|456", found.externalId)
  }

  @Test
  fun `update user then findById reflects changes`() {
    val user = User(
      externalId = "github|789",
      email = "old@example.com",
      displayName = "Old Name",
    )

    val created = repository.create(user)
    val updated = repository.update(
      created.copy(
        email = "new@example.com",
        displayName = "New Name",
        avatarUrl = "https://example.com/new-avatar.png",
      ),
    )

    val found = repository.findById(created.id!!)
    assertNotNull(found)
    assertEquals("new@example.com", found.email)
    assertEquals("New Name", found.displayName)
    assertEquals("https://example.com/new-avatar.png", found.avatarUrl)
  }

  @Test
  fun `findById with nonexistent id returns null`() {
    val found = repository.findById(999999L)
    assertNull(found)
  }

  @Test
  fun `inserting duplicate external_id fails with constraint violation`() {
    val user1 = User(
      externalId = "github|duplicate",
      email = "first@example.com",
      displayName = "First User",
    )
    repository.create(user1)

    val user2 = User(
      externalId = "github|duplicate",
      email = "second@example.com",
      displayName = "Second User",
    )

    assertThrows<Exception> {
      repository.create(user2)
    }
  }
}
