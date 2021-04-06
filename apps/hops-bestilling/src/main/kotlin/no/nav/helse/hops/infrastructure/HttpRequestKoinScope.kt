package no.nav.helse.hops.infrastructure

import org.koin.core.component.KoinScopeComponent
import org.koin.core.scope.Scope

class HttpRequestKoinScope : KoinScopeComponent {
    override val scope: Scope by lazy { getKoin().createScope<HttpRequestKoinScope>() }
}