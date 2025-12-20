package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.resources.ResourceLoader
import misk.security.authz.Unauthenticated
import misk.web.Get
import misk.web.Response
import misk.web.ResponseBody
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import misk.web.toResponseBody
import okhttp3.Headers.Companion.headersOf

@Singleton
public class IndexAction @Inject constructor(
    private val resourceLoader: ResourceLoader
) : WebAction {
    @Get("/")
    @Unauthenticated
    @ResponseContentType(MediaTypes.TEXT_HTML)
    fun index(): Response<ResponseBody> {
        val html = resourceLoader.utf8("classpath:/web/index.html")
            ?: return Response(
                body = "Not Found".toResponseBody(),
                statusCode = 404
            )
        return Response(
            body = html.toResponseBody(),
            headers = headersOf("Content-Type", MediaTypes.TEXT_HTML)
        )
    }
}
