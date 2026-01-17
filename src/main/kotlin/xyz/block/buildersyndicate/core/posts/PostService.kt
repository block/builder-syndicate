package xyz.block.buildersyndicate.core.posts

class PostService(
  private val postRepository: PostRepository,
  private val markdownRenderer: MarkdownRenderer,
) {
  fun create(authorId: Long, title: String, body: String): Post {
    val bodyHtml = markdownRenderer.render(body)
    val post = Post(
      authorId = authorId,
      title = title,
      body = body,
      bodyHtml = bodyHtml,
    )
    return postRepository.create(post)
  }

  fun update(postId: Long, requestingUserId: Long, title: String, body: String): Post {
    val existing = postRepository.findById(postId)
      ?: throw IllegalArgumentException("Post not found: $postId")

    if (existing.authorId != requestingUserId) {
      throw IllegalStateException("Only the author can update this post")
    }

    val bodyHtml = markdownRenderer.render(body)
    val updated = existing.copy(
      title = title,
      body = body,
      bodyHtml = bodyHtml,
    )
    return postRepository.update(updated)
  }

  fun delete(postId: Long, requestingUserId: Long) {
    val existing = postRepository.findById(postId)
      ?: throw IllegalArgumentException("Post not found: $postId")

    if (existing.authorId != requestingUserId) {
      throw IllegalStateException("Only the author can delete this post")
    }

    postRepository.delete(postId)
  }

  fun getById(id: Long): Post? = postRepository.findById(id)

  fun listRecent(limit: Int = 20): List<Post> = postRepository.listRecent(limit)
}
