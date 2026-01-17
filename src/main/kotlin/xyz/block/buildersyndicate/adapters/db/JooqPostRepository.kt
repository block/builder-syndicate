package xyz.block.buildersyndicate.adapters.db

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jooq.DSLContext
import xyz.block.buildersyndicate.adapters.db.jooq.tables.Posts.Companion.POSTS
import xyz.block.buildersyndicate.adapters.db.jooq.tables.records.PostsRecord
import xyz.block.buildersyndicate.core.posts.Post
import xyz.block.buildersyndicate.core.posts.PostRepository
import java.time.ZoneId

@Singleton
public class JooqPostRepository @Inject constructor(
  private val dsl: DSLContext,
) : PostRepository {

  override fun findById(id: Long): Post? {
    return dsl.selectFrom(POSTS)
      .where(POSTS.ID.eq(id))
      .fetchOne()
      ?.toPost()
  }

  override fun create(post: Post): Post {
    val record = dsl.newRecord(POSTS).apply {
      authorId = post.authorId
      title = post.title
      body = post.body
      bodyHtml = post.bodyHtml
    }
    record.store()
    record.refresh()
    return record.toPost()
  }

  override fun update(post: Post): Post {
    val record = dsl.selectFrom(POSTS)
      .where(POSTS.ID.eq(post.id))
      .fetchOne() ?: throw IllegalArgumentException("Post not found: ${post.id}")

    record.apply {
      authorId = post.authorId
      title = post.title
      body = post.body
      bodyHtml = post.bodyHtml
    }
    record.store()
    return record.toPost()
  }

  override fun delete(id: Long): Boolean {
    return dsl.deleteFrom(POSTS)
      .where(POSTS.ID.eq(id))
      .execute() > 0
  }

  override fun listByAuthor(authorId: Long): List<Post> {
    return dsl.selectFrom(POSTS)
      .where(POSTS.AUTHOR_ID.eq(authorId))
      .orderBy(POSTS.CREATED_AT.desc())
      .fetch()
      .map { it.toPost() }
  }

  override fun listRecent(limit: Int): List<Post> {
    return dsl.selectFrom(POSTS)
      .orderBy(POSTS.CREATED_AT.desc())
      .limit(limit)
      .fetch()
      .map { it.toPost() }
  }

  private fun PostsRecord.toPost(): Post = Post(
    id = this.id,
    authorId = this.authorId!!,
    title = this.title!!,
    body = this.body!!,
    bodyHtml = this.bodyHtml ?: "",
    createdAt = this.createdAt?.atZone(ZoneId.of("UTC"))?.toInstant(),
    updatedAt = this.updatedAt?.atZone(ZoneId.of("UTC"))?.toInstant(),
  )
}
