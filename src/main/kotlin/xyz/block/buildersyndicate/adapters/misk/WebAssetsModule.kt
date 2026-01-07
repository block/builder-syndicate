package xyz.block.buildersyndicate.adapters.misk

import misk.inject.KAbstractModule
import misk.web.WebActionModule
import misk.web.dashboard.WebTabResourceModule
import xyz.block.buildersyndicate.adapters.misk.actions.RootRedirectAction

class WebAssetsModule(
    private val isDevelopment: Boolean = false
) : KAbstractModule() {
    override fun configure() {
        install(WebActionModule.create<RootRedirectAction>())

        install(
            WebTabResourceModule(
                isDevelopment = isDevelopment,
                slug = "app",
                url_path_prefix = "/app/",
                resourcePath = "classpath:/web/",
                web_proxy_url = "http://localhost:5173/",
            )
        )
    }
}
