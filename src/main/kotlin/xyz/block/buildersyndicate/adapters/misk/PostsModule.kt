package xyz.block.buildersyndicate.adapters.misk

import com.google.inject.Provides
import jakarta.inject.Singleton
import misk.inject.KAbstractModule
import misk.web.WebActionModule
import xyz.block.buildersyndicate.adapters.markdown.SanitizingMarkdownRenderer
import xyz.block.buildersyndicate.adapters.misk.actions.posts.PostsAction
import xyz.block.buildersyndicate.core.posts.MarkdownRenderer
import xyz.block.buildersyndicate.core.posts.PostRepository
import xyz.block.buildersyndicate.core.posts.PostService

class PostsModule : KAbstractModule() {
  override fun configure() {
    bind<MarkdownRenderer>().to<SanitizingMarkdownRenderer>()

    install(WebActionModule.create<PostsAction>())
  }

  @Provides
  @Singleton
  fun providePostService(postRepository: PostRepository, markdownRenderer: MarkdownRenderer): PostService =
    PostService(postRepository, markdownRenderer)
}
