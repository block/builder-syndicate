package xyz.block.buildersyndicate.core.posts

interface PostRepository {
  fun findById(id: Long): Post?
  fun create(post: Post): Post
  fun update(post: Post): Post
  fun delete(id: Long): Boolean
  fun listByAuthor(authorId: Long): List<Post>
  fun listRecent(limit: Int = 20): List<Post>
}
