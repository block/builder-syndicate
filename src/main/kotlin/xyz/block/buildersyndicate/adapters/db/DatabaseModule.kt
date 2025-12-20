package xyz.block.buildersyndicate.adapters.db

import misk.inject.KAbstractModule
import xyz.block.buildersyndicate.core.users.UserRepository

public class DatabaseModule : KAbstractModule() {
    override fun configure() {
        bind<UserRepository>().to<JooqUserRepository>()
    }
}
