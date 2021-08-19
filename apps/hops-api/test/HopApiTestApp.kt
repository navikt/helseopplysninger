import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.koin.core.module.Module

val oAuthMock = MockOAuth2Server()
internal fun startOAuth() = with(oAuthMock, MockOAuth2Server::start)
internal fun stopOAuth() = with(oAuthMock, MockOAuth2Server::shutdown)

internal fun Application.doConfig(
    oauth: MockOAuth2Server,
    acceptedIssuer: String = "default",
    acceptedAudience: String = "default"
) {
    (environment.config as MapApplicationConfig).apply {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", acceptedIssuer)
        put("no.nav.security.jwt.issuers.0.discoveryurl", "${oauth.wellKnownUrl(acceptedIssuer)}")
        put("no.nav.security.jwt.issuers.0.accepted_audience", acceptedAudience)
        put("security.scopes.publish", "/test-publish")
        put("security.scopes.subscribe", "/test-subscribe")
    }
}

fun <R> withHopsTestApplication(testKoinModule: Module = Module(), test: TestApplicationEngine.() -> R): R {
    return withTestApplication({
        doConfig(oAuthMock)
        module(testKoinModule)
    }) {
        test()
    }
}

internal class KotestSetup() : AbstractProjectConfig() {
    override fun listeners(): List<Listener> = super.listeners() + KotestListener()
}

internal class KotestListener : ProjectListener {
    override suspend fun beforeProject() = startOAuth()
    override suspend fun afterProject() = stopOAuth()
}
