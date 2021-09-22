package fileshare

import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.test.HopsOAuthMock
import no.nav.helse.hops.test.HopsOAuthMock.MaskinportenScopes

fun <R> withFileshareTestApp(
    test: TestApplicationEngine.() -> R
): R {
    return withTestApplication(
        {
            config()
            module()
        },
        test
    )
}

internal class KotestSetup : AbstractProjectConfig() {
    override fun listeners(): List<Listener> = super.listeners() + KotestListener()
}

internal class KotestListener : ProjectListener {

    override suspend fun beforeProject() {
        MockServers.gcs.start()
        MockServers.gcpMetadata.start()
        MockServers.virusScanner.start()
        MockServers.oAuth.start()
    }
    override suspend fun afterProject() {
        MockServers.oAuth.shutdown()
        MockServers.gcs.shutdown()
        MockServers.gcpMetadata.shutdown()
        MockServers.virusScanner.shutdown()
    }
}

private fun Application.config() = (environment.config as MapApplicationConfig).apply {
    put("oauth.azure.name", MockServers.oAuth.azureIssuer())
    put("oauth.azure.discoveryUrl", "${MockServers.oAuth.azureWellKnownUrl()}")
    put("oauth.azure.audience", "default")

    put("oauth.maskinporten.issuer.name", MockServers.oAuth.maskinportenIssuer())
    put("oauth.maskinporten.issuer.discoveryUrl", "${MockServers.oAuth.maskinportenWellKnownUrl()}")
    put("oauth.maskinporten.issuer.audience", "default")
    put("oauth.maskinporten.uploadScope", MaskinportenScopes.WRITE.value)
    put("oauth.maskinporten.downloadScope", MaskinportenScopes.READ.value)

    put("fileStore.baseUrl", MockServers.gcs.getBaseUrl())
    put("fileStore.requiresAuth", "true")
    put("fileStore.virusScanningEnabled", "true")
    put(
        "fileStore.tokenFetchUrl",
        "${MockServers.gcpMetadata.getBaseUrl()}/computeMetadata/v1/instance/service-accounts/default/token"
    )
    put("fileStore.virusScannerUrl", "${MockServers.virusScanner.getBaseUrl()}/scan")
}
