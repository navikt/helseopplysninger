import api.module
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.kotest.extensions.system.withEnvironment
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server

internal fun Application.config(): MapApplicationConfig {

    return (environment.config as MapApplicationConfig).apply {

        put("oauth.maskinporten.name", "default")
        put("oauth.maskinporten.discoveryUrl", "${MockServers.oAuth.wellKnownUrl("default")}")
        put("oauth.maskinporten.audience", "default")
        put("oauth.publishScope", "/test-publish")
        put("oauth.subscribeScope", "/test-subscribe")
    }
}

internal fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R =
    withEnvironment(mapOf(
        "HOPS_EVENTSTORE_BASE_URL" to MockServers.eventStore.getBaseUrl(),
        "AZURE_APP_WELL_KNOWN_URL" to MockServers.oAuth.wellKnownUrl("azure").toString()
    )) {
        withTestApplication(
            {
                config()
                module()
            },
            test = test
        )
    }

internal class KotestSetup() : AbstractProjectConfig() {
    override fun listeners(): List<Listener> = super.listeners() + KotestListener()
}

internal class KotestListener : ProjectListener {
    override suspend fun beforeProject() {
        startOAuth()
        MockServers.eventStore.start()
    }
    override suspend fun afterProject() {
        stopOAuth()
        MockServers.eventStore.shutdown()
    }
}
internal fun startOAuth() = with(MockServers.oAuth, MockOAuth2Server::start)
internal fun stopOAuth() = with(MockServers.oAuth, MockOAuth2Server::shutdown)