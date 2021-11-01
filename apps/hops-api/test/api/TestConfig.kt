package api

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes
import uk.org.webcompere.systemstubs.environment.EnvironmentVariables

internal fun Application.config(): MapApplicationConfig {

    return (environment.config as MapApplicationConfig).apply {

        put("oauth.maskinporten.issuer.name", MockServers.oAuth.maskinportenIssuer())
        put("oauth.maskinporten.issuer.discoveryUrl", "${MockServers.oAuth.maskinportenWellKnownUrl()}")
        put("oauth.maskinporten.issuer.audience", "default")
        put("oauth.maskinporten.writeScope", MaskinportenScopes.WRITE.value)
        put("oauth.maskinporten.readScope", MaskinportenScopes.READ.value)
    }
}

fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R =
    EnvironmentVariables(config).execute<R> {
        withTestApplication(
            {
                config()
                module()
            },
            test = test
        )
    }

private val config = mapOf(
    "HOPS_EVENTSTORE_BASE_URL" to MockServers.eventStore.getBaseUrl(),
    "AZURE_APP_WELL_KNOWN_URL" to MockServers.oAuth.azureWellKnownUrl().toString()
)
