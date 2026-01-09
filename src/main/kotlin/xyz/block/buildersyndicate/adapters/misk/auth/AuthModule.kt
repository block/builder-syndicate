package xyz.block.buildersyndicate.adapters.misk.auth

import com.google.inject.Provides
import jakarta.inject.Singleton
import misk.inject.KAbstractModule
import misk.scope.ActionScoped
import misk.web.HttpCall
import misk.web.WebActionModule
import xyz.block.buildersyndicate.adapters.misk.actions.LoginAction
import xyz.block.buildersyndicate.adapters.misk.actions.LogoutAction
import xyz.block.buildersyndicate.core.users.UserRepository

public class AuthModule : KAbstractModule() {
    override fun configure() {
        bind<SessionManager>().asEagerSingleton()
        
        install(WebActionModule.create<LoginAction>())
        install(WebActionModule.create<LogoutAction>())
    }

    @Provides
    @Singleton
    fun provideCurrentUser(
        httpCall: ActionScoped<HttpCall>,
        sessionManager: SessionManager,
        userRepository: UserRepository
    ): CurrentUser {
        val call = httpCall.get()
        val cookies = call.requestHeaders["Cookie"] ?: return CurrentUser(null)
        
        val sessionToken = cookies.split(";")
            .map { it.trim() }
            .find { it.startsWith("${SessionManager.COOKIE_NAME}=") }
            ?.substringAfter("=")
            ?: return CurrentUser(null)

        val userId = sessionManager.getUserId(sessionToken) ?: return CurrentUser(null)
        return CurrentUser(userRepository.findById(userId))
    }
}
