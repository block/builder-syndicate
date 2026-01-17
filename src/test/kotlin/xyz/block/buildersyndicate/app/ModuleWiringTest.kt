package xyz.block.buildersyndicate.app

import com.google.inject.Guice
import org.jooq.DSLContext
import org.junit.jupiter.api.Test
import xyz.block.buildersyndicate.adapters.db.DatabaseModule
import xyz.block.buildersyndicate.adapters.misk.auth.SessionManager
import xyz.block.buildersyndicate.core.users.UserRepository
import kotlin.test.assertNotNull

class ModuleWiringTest {

  @Test
  fun `DatabaseModule provides UserRepository and DSLContext`() {
    val injector = Guice.createInjector(DatabaseModule())

    val userRepo = injector.getInstance(UserRepository::class.java)
    assertNotNull(userRepo)

    val dsl = injector.getInstance(DSLContext::class.java)
    assertNotNull(dsl)
  }

  @Test
  fun `SessionManager is instantiable`() {
    val sessionManager = SessionManager()
    assertNotNull(sessionManager)
  }
}
