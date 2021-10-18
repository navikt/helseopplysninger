package fileshare

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
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
