package xyz.block.buildersyndicate.adapters.db

import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import xyz.block.buildersyndicate.adapters.db.jooq.tables.Posts.Companion.POSTS
import xyz.block.buildersyndicate.adapters.db.jooq.tables.Users.Companion.USERS
import xyz.block.buildersyndicate.core.posts.Post
import xyz.block.buildersyndicate.core.users.User
import java.sql.Connection
import java.sql.DriverManager
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

class JooqPostRepositoryTest {
  private lateinit var connection: Connection
  private lateinit var dsl: DSLContext
  private lateinit var postRepository: JooqPostRepository
  private lateinit var userRepository: JooqUserRepository
  private lateinit var testUser: User

  @BeforeEach
  fun setup() {
    connection = DriverManager.getConnection(
      "jdbc:mysql://localhost:3307/buildersyndicate",
      "root",
      "root",
    )
    dsl = DSL.using(connection, SQLDialect.MYSQL)
    postRepository = JooqPostRepository(dsl)
    userRepository = JooqUserRepository(dsl)
    dsl.deleteFrom(POSTS).execute()
    dsl.deleteFrom(USERS).execute()
    testUser = userRepository.create(
      User(
        externalId = "test|user",
        email = "test@example.com",
        displayName = "Test User",
      ),
    )
  }

  @AfterEach
  fun teardown() {
    connection.close()
  }

  @Test
  fun `create post then findById returns it with matching fields`() {
    val post = Post(
      authorId = testUser.id!!,
      title = "Test Post Title",
      body = "This is the body of the test post.",
    )

    val created = postRepository.create(post)

    assertNotNull(created.id)
    assertNotNull(created.createdAt)
    assertNotNull(created.updatedAt)

    val found = postRepository.findById(created.id!!)
    assertNotNull(found)
    assertEquals(created.id, found.id)
    assertEquals(testUser.id, found.authorId)
    assertEquals("Test Post Title", found.title)
    assertEquals("This is the body of the test post.", found.body)
  }

  @Test
  fun `update post then findById reflects changes`() {
    val post = Post(
      authorId = testUser.id!!,
      title = "Original Title",
      body = "Original body",
    )

    val created = postRepository.create(post)
    postRepository.update(
      created.copy(
        title = "Updated Title",
        body = "Updated body content",
      ),
    )

    val found = postRepository.findById(created.id!!)
    assertNotNull(found)
    assertEquals("Updated Title", found.title)
    assertEquals("Updated body content", found.body)
  }

  @Test
  fun `delete post then findById returns null`() {
    val post = Post(
      authorId = testUser.id!!,
      title = "Post to Delete",
      body = "This post will be deleted",
    )

    val created = postRepository.create(post)
    val deleted = postRepository.delete(created.id!!)

    assertTrue(deleted)
    assertNull(postRepository.findById(created.id!!))
  }

  @Test
  fun `listRecent returns posts ordered newest-first`() {
    val post1 = postRepository.create(
      Post(
        authorId = testUser.id!!,
        title = "First Post",
        body = "Created first",
      ),
    )
    Thread.sleep(1100)
    val post2 = postRepository.create(
      Post(
        authorId = testUser.id!!,
        title = "Second Post",
        body = "Created second",
      ),
    )
    Thread.sleep(1100)
    val post3 = postRepository.create(
      Post(
        authorId = testUser.id!!,
        title = "Third Post",
        body = "Created third",
      ),
    )

    val recent = postRepository.listRecent(10)

    assertEquals(3, recent.size)
    assertEquals("Third Post", recent[0].title)
    assertEquals("Second Post", recent[1].title)
    assertEquals("First Post", recent[2].title)
  }

  @Test
  fun `creating post with nonexistent authorId fails with FK constraint`() {
    val post = Post(
      authorId = 999999L,
      title = "Invalid Post",
      body = "This should fail",
    )

    assertThrows<Exception> {
      postRepository.create(post)
    }
  }

  @Test
  fun `findById with nonexistent id returns null`() {
    val found = postRepository.findById(999999L)
    assertNull(found)
  }

  @Test
  fun `listByAuthor returns only posts by that author`() {
    val otherUser = userRepository.create(
      User(
        externalId = "other|user",
        email = "other@example.com",
        displayName = "Other User",
      ),
    )

    postRepository.create(
      Post(
        authorId = testUser.id!!,
        title = "Test User Post",
        body = "By test user",
      ),
    )
    postRepository.create(
      Post(
        authorId = otherUser.id!!,
        title = "Other User Post",
        body = "By other user",
      ),
    )

    val testUserPosts = postRepository.listByAuthor(testUser.id!!)
    assertEquals(1, testUserPosts.size)
    assertEquals("Test User Post", testUserPosts[0].title)
  }
}
