package no.nav.helse.hops.koin

import org.koin.core.component.KoinScopeComponent
import org.koin.core.component.newScope
import org.koin.core.scope.Scope

class HttpRequestKoinScope : KoinScopeComponent {
    override val scope: Scope by newScope()
}
