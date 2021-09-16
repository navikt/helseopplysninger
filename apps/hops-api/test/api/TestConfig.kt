package api

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.kotest.extensions.system.withEnvironment
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.test.HopsOAuthMock
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes

internal fun Application.config(): MapApplicationConfig {

    return (environment.config as MapApplicationConfig).apply {

        put("oauth.maskinporten.issuer.name", HopsOAuthMock.MASKINPORTEN_ISSUER_NAME)
        put("oauth.maskinporten.issuer.discoveryUrl", "${MockServers.oAuth.maskinportenWellKnownUrl()}")
        put("oauth.maskinporten.issuer.audience", "default")
        put("oauth.maskinporten.writeScope", MaskinportenScopes.WRITE.value)
        put("oauth.maskinporten.readScope", MaskinportenScopes.READ.value)
    }
}

internal fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R =
    withEnvironment(
        mapOf(
            "HOPS_EVENTSTORE_BASE_URL" to MockServers.eventStore.getBaseUrl(),
            "AZURE_APP_WELL_KNOWN_URL" to MockServers.oAuth.azureWellKnownUrl().toString()
        )
    ) {
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
        MockServers.oAuth.start()
        MockServers.eventStore.start()
    }

    override suspend fun afterProject() {
        MockServers.oAuth.shutdown()
        MockServers.eventStore.shutdown()
    }
}
