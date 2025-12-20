package xyz.block.buildersyndicate.adapters.misk

import misk.inject.KAbstractModule
import misk.web.WebActionModule
import misk.web.resources.StaticResourceAction
import misk.web.resources.StaticResourceEntry
import xyz.block.buildersyndicate.adapters.misk.actions.IndexAction

public class WebAssetsModule : KAbstractModule() {
    override fun configure() {
        install(WebActionModule.create<IndexAction>())
        
        multibind<StaticResourceEntry>().toInstance(
            StaticResourceEntry(
                url_path_prefix = "/assets/",
                resourcePath = "classpath:/web/assets/"
            )
        )
        install(WebActionModule.createWithPrefix<StaticResourceAction>(url_path_prefix = "/assets/"))
    }
}
