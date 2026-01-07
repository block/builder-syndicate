package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.net.HttpURLConnection
import misk.security.authz.Unauthenticated
import misk.web.Get
import misk.web.Response
import misk.web.ResponseBody
import misk.web.actions.WebAction
import misk.web.toResponseBody
import okhttp3.Headers

@Singleton
class RootRedirectAction @Inject constructor() : WebAction {
    @Get("/")
    @Unauthenticated
    fun root(): Response<ResponseBody> {
        return Response(
            body = "go to /app/".toResponseBody(),
            statusCode = HttpURLConnection.HTTP_MOVED_TEMP,
            headers = Headers.headersOf("Location", "/app/"),
        )
    }
}
