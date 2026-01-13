package xyz.block.buildersyndicate.adapters.misk.actions

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.scope.ActionScoped
import misk.security.authz.Unauthenticated
import misk.web.Get
import misk.web.Response
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import xyz.block.buildersyndicate.adapters.misk.auth.CurrentUser
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

@Singleton
class WhoamiAction @Inject constructor(
  private val currentUser: ActionScoped<CurrentUser>,
) : WebAction {

  data class WhoamiResponse(
    val id: Long,
    val externalId: String,
    val email: String,
    val displayName: String,
    val avatarUrl: String?,
  )

  @Get("/api/v1/whoami")
  @Unauthenticated
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  fun whoami(): Response<WhoamiResponse> {
    val user = currentUser.get().user
      ?: return Response(
        body = WhoamiResponse(0, "", "", "", null),
        statusCode = HTTP_UNAUTHORIZED,
      )

    return Response(
      body = WhoamiResponse(
        id = user.id!!,
        externalId = user.externalId,
        email = user.email,
        displayName = user.displayName,
        avatarUrl = user.avatarUrl,
      ),
    )
  }
}
