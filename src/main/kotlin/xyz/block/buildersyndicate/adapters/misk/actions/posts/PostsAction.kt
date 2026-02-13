package xyz.block.buildersyndicate.adapters.misk.actions.posts

import jakarta.inject.Inject
import jakarta.inject.Singleton
import misk.scope.ActionScoped
import misk.security.authz.Unauthenticated
import misk.web.Delete
import misk.web.Get
import misk.web.PathParam
import misk.web.Post
import misk.web.Put
import misk.web.RequestBody
import misk.web.RequestContentType
import misk.web.Response
import misk.web.ResponseContentType
import misk.web.actions.WebAction
import misk.web.mediatype.MediaTypes
import xyz.block.buildersyndicate.adapters.misk.actions.ErrorResponse
import xyz.block.buildersyndicate.adapters.misk.auth.CurrentUser
import xyz.block.buildersyndicate.core.posts.PostService
import xyz.block.buildersyndicate.core.users.UserRepository
import java.net.HttpURLConnection.HTTP_CREATED
import java.net.HttpURLConnection.HTTP_FORBIDDEN
import java.net.HttpURLConnection.HTTP_NOT_FOUND
import java.net.HttpURLConnection.HTTP_NO_CONTENT
import java.net.HttpURLConnection.HTTP_OK
import java.net.HttpURLConnection.HTTP_UNAUTHORIZED

@Singleton
class PostsAction @Inject constructor(
  private val postService: PostService,
  private val userRepository: UserRepository,
  private val currentUser: ActionScoped<CurrentUser>,
) : WebAction {

  // -- Reads --

  @Get("/api/v1/posts")
  @Unauthenticated
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  fun list(): PostListResponse {
    val posts = postService.listRecent()
    return PostListResponse(posts.map { post ->
      PostResponse.from(post, userRepository.findById(post.authorId))
    })
  }

  @Get("/api/v1/posts/{id}")
  @Unauthenticated
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  fun get(@PathParam id: Long): Response<Any> {
    val post = postService.getById(id)
      ?: return notFound()

    return Response(
      body = PostResponse.from(post, userRepository.findById(post.authorId)),
      statusCode = HTTP_OK,
    )
  }

  // -- Writes (auth required) --

  @Post("/api/v1/posts")
  @Unauthenticated
  @RequestContentType(MediaTypes.APPLICATION_JSON)
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  fun create(@RequestBody request: CreatePostRequest): Response<Any> {
    val user = requireAuth() ?: return unauthorized()

    val post = postService.create(user.id!!, request.title, request.body)

    return Response(
      body = PostResponse.from(post, userRepository.findById(post.authorId)),
      statusCode = HTTP_CREATED,
    )
  }

  @Put("/api/v1/posts/{id}")
  @Unauthenticated
  @RequestContentType(MediaTypes.APPLICATION_JSON)
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  fun update(@PathParam id: Long, @RequestBody request: UpdatePostRequest): Response<Any> {
    val user = requireAuth() ?: return unauthorized()

    val existing = postService.getById(id) ?: return notFound()

    if (existing.authorId != user.id) {
      return forbidden("Only the author can update this post")
    }

    val updated = postService.update(id, user.id!!, request.title, request.body)

    return Response(
      body = PostResponse.from(updated, userRepository.findById(updated.authorId)),
      statusCode = HTTP_OK,
    )
  }

  @Delete("/api/v1/posts/{id}")
  @Unauthenticated
  @ResponseContentType(MediaTypes.APPLICATION_JSON)
  fun delete(@PathParam id: Long): Response<Any> {
    val user = requireAuth() ?: return unauthorized()

    val existing = postService.getById(id) ?: return notFound()

    if (existing.authorId != user.id) {
      return forbidden("Only the author can delete this post")
    }

    postService.delete(id, user.id!!)

    return Response(body = Unit, statusCode = HTTP_NO_CONTENT)
  }

  // -- Shared responses --

  private fun requireAuth() = currentUser.get().user

  private fun unauthorized() = Response<Any>(
    body = ErrorResponse("unauthorized", "Authentication required"),
    statusCode = HTTP_UNAUTHORIZED,
  )

  private fun notFound() = Response<Any>(
    body = ErrorResponse("not_found", "Post not found"),
    statusCode = HTTP_NOT_FOUND,
  )

  private fun forbidden(message: String) = Response<Any>(
    body = ErrorResponse("forbidden", message),
    statusCode = HTTP_FORBIDDEN,
  )
}
