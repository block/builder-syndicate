package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.security.authz.Unauthenticated
import misk.web.Get
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes

@Singleton
public class BusyAction @Inject constructor() : WebAction {
    @Get("/")
    @Unauthenticated
    @ResponseContentType(MediaTypes.TEXT_PLAIN_UTF8)
    fun busy(): String {
        return """
            ATDT buildersyndicate.block.xyz
            BUSY
            +++
            ATH0
            NO CARRIER
        """.trimIndent()
    }
}
