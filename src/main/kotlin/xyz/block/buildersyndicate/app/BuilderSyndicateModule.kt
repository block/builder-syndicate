package xyz.block.buildersyndicate.app

import misk.inject.KAbstractModule
import misk.web.WebActionModule
import xyz.block.buildersyndicate.adapters.misk.actions.BusyAction

public class BuilderSyndicateModule : KAbstractModule() {
    override fun configure() {
        install(WebActionModule.create<BusyAction>())
    }
}
