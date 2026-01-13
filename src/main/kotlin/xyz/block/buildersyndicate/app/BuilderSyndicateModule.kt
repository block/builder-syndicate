package xyz.block.buildersyndicate.app

import misk.inject.KAbstractModule
import xyz.block.buildersyndicate.adapters.misk.WebAssetsModule

public class BuilderSyndicateModule : KAbstractModule() {
  override fun configure() {
    install(WebAssetsModule())
  }
}
