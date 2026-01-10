package xyz.block.buildersyndicate.core.users

class FakeUserRepository(
    private val users: MutableMap<Long, User> = mutableMapOf(),
    private var nextId: Long = 1L
) : UserRepository {

    val createdUsers = mutableListOf<User>()

    override fun findById(id: Long): User? = users[id]

    override fun findByExternalId(externalId: String): User? =
        users.values.find { it.externalId == externalId }

    override fun create(user: User): User {
        val newUser = user.copy(id = nextId++)
        users[newUser.id!!] = newUser
        createdUsers.add(newUser)
        return newUser
    }

    override fun update(user: User): User {
        users[user.id!!] = user
        return user
    }

    fun addUser(user: User) {
        users[user.id!!] = user
    }
}
