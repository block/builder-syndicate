package xyz.block.buildersyndicate.adapters.misk.actions.posts

import java.time.Instant
import xyz.block.buildersyndicate.core.posts.Post
import xyz.block.buildersyndicate.core.users.User

data class PostResponse(
  val id: Long,
  val title: String,
  val body: String,
  val bodyHtml: String,
  val authorId: Long,
  val authorUsername: String,
  val createdAt: Instant,
  val updatedAt: Instant,
) {
  companion object {
    fun from(post: Post, author: User?): PostResponse = PostResponse(
      id = post.id!!,
      title = post.title,
      body = post.body,
      bodyHtml = post.bodyHtml,
      authorId = post.authorId,
      authorUsername = author?.displayName ?: "unknown",
      createdAt = post.createdAt!!,
      updatedAt = post.updatedAt!!,
    )
  }
}

data class PostListResponse(
  val posts: List<PostResponse>,
)

data class CreatePostRequest(
  val title: String,
  val body: String,
)

data class UpdatePostRequest(
  val title: String,
  val body: String,
)

data class ErrorResponse(
  val error: String,
  val message: String,
)
