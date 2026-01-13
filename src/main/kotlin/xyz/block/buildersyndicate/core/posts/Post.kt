package xyz.block.buildersyndicate.core.posts

import java.time.Instant

data class Post(
  val id: Long? = null,
  val authorId: Long,
  val title: String,
  val body: String,
  val createdAt: Instant? = null,
  val updatedAt: Instant? = null,
)
