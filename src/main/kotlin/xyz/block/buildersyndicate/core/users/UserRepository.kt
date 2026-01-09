package xyz.block.buildersyndicate.core.users

interface UserRepository {
    fun findById(id: Long): User?
    fun findByExternalId(externalId: String): User?
    fun create(user: User): User
    fun update(user: User): User
}
