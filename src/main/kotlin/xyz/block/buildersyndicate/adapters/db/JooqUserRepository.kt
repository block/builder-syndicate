package xyz.block.buildersyndicate.adapters.db

import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.jooq.DSLContext
import xyz.block.buildersyndicate.adapters.db.jooq.tables.Users.Companion.USERS
import xyz.block.buildersyndicate.adapters.db.jooq.tables.records.UsersRecord
import xyz.block.buildersyndicate.core.users.User
import xyz.block.buildersyndicate.core.users.UserRepository
import java.time.ZoneId

@Singleton
public class JooqUserRepository @Inject constructor(
    private val dsl: DSLContext
) : UserRepository {

    override fun findById(id: Long): User? {
        return dsl.selectFrom(USERS)
            .where(USERS.ID.eq(id))
            .fetchOne()
            ?.toUser()
    }

    override fun findByExternalId(externalId: String): User? {
        return dsl.selectFrom(USERS)
            .where(USERS.EXTERNAL_ID.eq(externalId))
            .fetchOne()
            ?.toUser()
    }

    override fun create(user: User): User {
        val record = dsl.newRecord(USERS).apply {
            externalId = user.externalId
            email = user.email
            displayName = user.displayName
            avatarUrl = user.avatarUrl
        }
        record.store()
        record.refresh()
        return record.toUser()
    }

    override fun update(user: User): User {
        val record = dsl.selectFrom(USERS)
            .where(USERS.ID.eq(user.id))
            .fetchOne() ?: throw IllegalArgumentException("User not found: ${user.id}")

        record.apply {
            externalId = user.externalId
            email = user.email
            displayName = user.displayName
            avatarUrl = user.avatarUrl
        }
        record.store()
        return record.toUser()
    }

    private fun UsersRecord.toUser(): User = User(
        id = this.id,
        externalId = this.externalId!!,
        email = this.email!!,
        displayName = this.displayName!!,
        avatarUrl = this.avatarUrl,
        createdAt = this.createdAt?.atZone(ZoneId.of("UTC"))?.toInstant(),
        updatedAt = this.updatedAt?.atZone(ZoneId.of("UTC"))?.toInstant()
    )
}
