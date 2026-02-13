package xyz.block.buildersyndicate.adapters.misk.actions.posts

import misk.scope.ActionScoped
import org.junit.jupiter.api.Test
import xyz.block.buildersyndicate.adapters.misk.auth.CurrentUser
import xyz.block.buildersyndicate.core.posts.MarkdownRenderer
import xyz.block.buildersyndicate.core.posts.Post
import xyz.block.buildersyndicate.core.posts.PostRepository
import xyz.block.buildersyndicate.core.posts.PostService
import xyz.block.buildersyndicate.core.users.User
import xyz.block.buildersyndicate.core.users.UserRepository
import xyz.block.buildersyndicate.protos.v1.CreatePostRequest
import xyz.block.buildersyndicate.protos.v1.PostListResponse
import xyz.block.buildersyndicate.protos.v1.PostResponse
import xyz.block.buildersyndicate.protos.v1.UpdatePostRequest
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED
import java.time.Instant
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

class PostsActionTest {

  private val alice = User(
    id = 1L,
    externalId = "dev-alice",
    email = "alice@example.com",
    displayName = "Alice",
  )

  private val bob = User(
    id = 2L,
    externalId = "dev-bob",
    email = "bob@example.com",
    displayName = "Bob",
  )

  private class FakeMarkdownRenderer : MarkdownRenderer {
    override fun render(markdown: String): String = "<p>$markdown</p>"
  }

  private class FakePostRepository : PostRepository {
    private val posts = mutableMapOf<Long, Post>()
    private var nextId = 1L

    override fun findById(id: Long): Post? = posts[id]

    override fun create(post: Post): Post {
      val id = nextId++
      val now = Instant.now()
      val created = post.copy(id = id, createdAt = now, updatedAt = now)
      posts[id] = created
      return created
    }

    override fun update(post: Post): Post {
      val id = post.id ?: throw IllegalArgumentException("Post must have an id")
      val updated = post.copy(updatedAt = Instant.now())
      posts[id] = updated
      return updated
    }

    override fun delete(id: Long): Boolean = posts.remove(id) != null

    override fun listByAuthor(authorId: Long): List<Post> =
      posts.values.filter { it.authorId == authorId }

    override fun listRecent(limit: Int): List<Post> =
      posts.values.sortedByDescending { it.createdAt }.take(limit)
  }

  private class FakeUserRepository(vararg users: User) : UserRepository {
    private val users = users.associateBy { it.id!! }.toMutableMap()
    private var nextId = (users.maxOfOrNull { it.id!! } ?: 0) + 1

    override fun findById(id: Long): User? = users[id]
    override fun findByExternalId(externalId: String): User? =
      users.values.find { it.externalId == externalId }

    override fun create(user: User): User {
      val id = nextId++
      val created = user.copy(id = id, createdAt = Instant.now(), updatedAt = Instant.now())
      users[id] = created
      return created
    }

    override fun update(user: User): User {
      val id = user.id ?: throw IllegalArgumentException("User must have an id")
      users[id] = user
      return user
    }
  }

  private fun actionScopedOf(user: User?): ActionScoped<CurrentUser> {
    return object : ActionScoped<CurrentUser> {
      override fun get(): CurrentUser = CurrentUser(user)
    }
  }

  private fun action(
    currentUser: User? = null,
    vararg users: User = arrayOf(alice, bob),
  ): PostsAction {
    val userRepo = FakeUserRepository(*users)
    val postService = PostService(FakePostRepository(), FakeMarkdownRenderer())
    return PostsAction(postService, userRepo, actionScopedOf(currentUser))
  }

  private fun actionWithService(
    currentUser: User? = null,
    vararg users: User = arrayOf(alice, bob),
  ): Pair<PostsAction, PostService> {
    val userRepo = FakeUserRepository(*users)
    val postRepo = FakePostRepository()
    val postService = PostService(postRepo, FakeMarkdownRenderer())
    return PostsAction(postService, userRepo, actionScopedOf(currentUser)) to postService
  }

  // -- list --

  @Test
  fun `list returns empty when no posts exist`() {
    val action = action()

    val response = action.list()

    assertTrue(response.posts.isEmpty())
  }

  @Test
  fun `list returns posts with author names`() {
    val (action, service) = actionWithService(currentUser = alice)
    service.create(alice.id!!, "First", "body one")
    service.create(alice.id!!, "Second", "body two")

    val response = action.list()

    assertEquals(2, response.posts.size)
    assertEquals("Second", response.posts[0].title)
    assertEquals("First", response.posts[1].title)
    assertEquals("Alice", response.posts[0].author_username)
  }

  // -- get --

  @Test
  fun `get returns 404 for nonexistent post`() {
    val action = action()

    val response = action.get(999L)

    assertEquals(HTTP_NOT_FOUND, response.statusCode)
  }

  @Test
  fun `get returns post with rendered HTML`() {
    val (action, service) = actionWithService()
    val created = service.create(alice.id!!, "Title", "# Hello")

    val response = action.get(created.id!!)

    assertEquals(HTTP_OK, response.statusCode)
    val body = response.body as PostResponse
    assertEquals("Title", body.title)
    assertEquals("<p># Hello</p>", body.body_html)
    assertEquals("Alice", body.author_username)
  }

  // -- create --

  @Test
  fun `create returns 401 when not authenticated`() {
    val action = action(currentUser = null)

    val response = action.create(CreatePostRequest(title = "Title", body = "Body"))

    assertEquals(HTTP_UNAUTHORIZED, response.statusCode)
  }

  @Test
  fun `create returns 201 with post response`() {
    val action = action(currentUser = alice)

    val response = action.create(CreatePostRequest(title = "My Post", body = "# Content"))

    assertEquals(HTTP_CREATED, response.statusCode)
    val body = response.body as PostResponse
    assertEquals("My Post", body.title)
    assertEquals("<p># Content</p>", body.body_html)
    assertEquals(alice.id, body.author_id)
    assertEquals("Alice", body.author_username)
    assertNotNull(body.id)
  }

  // -- update --

  @Test
  fun `update returns 401 when not authenticated`() {
    val action = action(currentUser = null)

    val response = action.update(1L, UpdatePostRequest(title = "New", body = "Body"))

    assertEquals(HTTP_UNAUTHORIZED, response.statusCode)
  }

  @Test
  fun `update returns 404 for nonexistent post`() {
    val action = action(currentUser = alice)

    val response = action.update(999L, UpdatePostRequest(title = "New", body = "Body"))

    assertEquals(HTTP_NOT_FOUND, response.statusCode)
  }

  @Test
  fun `update returns 403 when not the author`() {
    val (action, service) = actionWithService(currentUser = bob)
    val post = service.create(alice.id!!, "Alice's Post", "Content")

    val response = action.update(post.id!!, UpdatePostRequest(title = "Hijacked", body = "Nope"))

    assertEquals(HTTP_FORBIDDEN, response.statusCode)
  }

  @Test
  fun `update returns 200 with updated post`() {
    val (action, service) = actionWithService(currentUser = alice)
    val post = service.create(alice.id!!, "Original", "Old body")

    val response = action.update(post.id!!, UpdatePostRequest(title = "Updated", body = "New body"))

    assertEquals(HTTP_OK, response.statusCode)
    val body = response.body as PostResponse
    assertEquals("Updated", body.title)
    assertEquals("<p>New body</p>", body.body_html)
  }

  // -- delete --

  @Test
  fun `delete returns 401 when not authenticated`() {
    val action = action(currentUser = null)

    val response = action.delete(1L)

    assertEquals(HTTP_UNAUTHORIZED, response.statusCode)
  }

  @Test
  fun `delete returns 404 for nonexistent post`() {
    val action = action(currentUser = alice)

    val response = action.delete(999L)

    assertEquals(HTTP_NOT_FOUND, response.statusCode)
  }

  @Test
  fun `delete returns 403 when not the author`() {
    val (action, service) = actionWithService(currentUser = bob)
    val post = service.create(alice.id!!, "Alice's Post", "Content")

    val response = action.delete(post.id!!)

    assertEquals(HTTP_FORBIDDEN, response.statusCode)
  }

  @Test
  fun `delete returns 204 and removes post`() {
    val (action, service) = actionWithService(currentUser = alice)
    val post = service.create(alice.id!!, "Doomed", "Content")

    val response = action.delete(post.id!!)

    assertEquals(HTTP_NO_CONTENT, response.statusCode)
    assertEquals(HTTP_NOT_FOUND, action.get(post.id!!).statusCode)
  }
}
