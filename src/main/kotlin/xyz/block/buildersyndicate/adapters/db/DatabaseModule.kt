package xyz.block.buildersyndicate.adapters.db

import com.mysql.cj.jdbc.MysqlDataSource
import jakarta.inject.Singleton
import misk.inject.KAbstractModule
import org.jooq.DSLContext
import org.jooq.SQLDialect
import org.jooq.impl.DSL
import xyz.block.buildersyndicate.core.users.UserRepository

public class DatabaseModule : KAbstractModule() {
  override fun configure() {
    bind<UserRepository>().to<JooqUserRepository>()
  }

  @com.google.inject.Provides
  @Singleton
  fun provideDSLContext(): DSLContext {
    val dataSource = MysqlDataSource().apply {
      setUrl("jdbc:mysql://localhost:3307/buildersyndicate")
      user = "root"
      password = "root"
    }
    return DSL.using(dataSource, SQLDialect.MYSQL)
  }
}
