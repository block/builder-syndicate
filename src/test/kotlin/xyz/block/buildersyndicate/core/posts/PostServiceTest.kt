package xyz.block.buildersyndicate.core.posts

import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith
import kotlin.test.assertNotNull

class PostServiceTest {

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

    override fun delete(id: Long): Boolean {
      return posts.remove(id) != null
    }

    override fun listByAuthor(authorId: Long): List<Post> = posts.values.filter { it.authorId == authorId }

    override fun listRecent(limit: Int): List<Post> = posts.values.sortedByDescending { it.createdAt }.take(limit)
  }

  @Test
  fun `create post with markdown body produces HTML`() {
    val service = PostService(FakePostRepository(), FakeMarkdownRenderer())

    val post = service.create(authorId = 1L, title = "Test", body = "Hello **world**")

    assertNotNull(post.id)
    assertEquals("Hello **world**", post.body)
    assertEquals("<p>Hello **world**</p>", post.bodyHtml)
  }

  @Test
  fun `update post re-renders markdown to HTML`() {
    val service = PostService(FakePostRepository(), FakeMarkdownRenderer())
    val created = service.create(authorId = 1L, title = "Test", body = "Original")

    val updated = service.update(
      postId = created.id!!,
      requestingUserId = 1L,
      title = "Updated Title",
      body = "Updated body",
    )

    assertEquals("Updated body", updated.body)
    assertEquals("<p>Updated body</p>", updated.bodyHtml)
  }

  @Test
  fun `only post author can update their post`() {
    val service = PostService(FakePostRepository(), FakeMarkdownRenderer())
    val created = service.create(authorId = 1L, title = "Test", body = "Content")

    val exception = assertFailsWith<IllegalStateException> {
      service.update(
        postId = created.id!!,
        requestingUserId = 999L,
        title = "Hacked",
        body = "Hacked content",
      )
    }

    assertEquals("Only the author can update this post", exception.message)
  }

  @Test
  fun `only post author can delete their post`() {
    val service = PostService(FakePostRepository(), FakeMarkdownRenderer())
    val created = service.create(authorId = 1L, title = "Test", body = "Content")

    val exception = assertFailsWith<IllegalStateException> {
      service.delete(postId = created.id!!, requestingUserId = 999L)
    }

    assertEquals("Only the author can delete this post", exception.message)
  }

  @Test
  fun `author can delete their own post`() {
    val repository = FakePostRepository()
    val service = PostService(repository, FakeMarkdownRenderer())
    val created = service.create(authorId = 1L, title = "Test", body = "Content")

    service.delete(postId = created.id!!, requestingUserId = 1L)

    assertEquals(null, service.getById(created.id!!))
  }
}
