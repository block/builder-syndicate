package xyz.block.buildersyndicate.app

import misk.inject.KAbstractModule
import xyz.block.buildersyndicate.adapters.db.DatabaseModule
import xyz.block.buildersyndicate.adapters.misk.WebAssetsModule
import xyz.block.buildersyndicate.adapters.misk.auth.AuthModule

public class BuilderSyndicateModule : KAbstractModule() {
  override fun configure() {
    install(DatabaseModule())
    install(WebAssetsModule())
    install(AuthModule())
  }
}
