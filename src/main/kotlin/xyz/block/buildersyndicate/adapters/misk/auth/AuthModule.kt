package xyz.block.buildersyndicate.adapters.misk.auth

import misk.scope.ActionScopedProviderModule
import misk.web.WebActionModule
import xyz.block.buildersyndicate.adapters.misk.actions.LogoutAction
import xyz.block.buildersyndicate.adapters.misk.actions.UnsafeDevLoginAction
import xyz.block.buildersyndicate.adapters.misk.actions.WhoamiAction

class AuthModule : ActionScopedProviderModule() {
    override fun configureProviders() {
        bind<SessionManager>().asEagerSingleton()

        install(WebActionModule.create<UnsafeDevLoginAction>())
        install(WebActionModule.create<LogoutAction>())
        install(WebActionModule.create<WhoamiAction>())

        bindProvider(CurrentUser::class, CurrentUserProvider::class)
    }
}
