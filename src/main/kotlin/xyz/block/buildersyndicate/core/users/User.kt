package xyz.block.buildersyndicate.core.users

import java.time.Instant

data class User(
    val id: Long? = null,
    val externalId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: Instant? = null,
    val updatedAt: Instant? = null
)
