
import fileshare.module
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.security.mock.oauth2.MockOAuth2Server

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
        startOAuth()
    }
    override suspend fun afterProject() {
        stopOAuth()
        MockServers.gcs.shutdown()
        MockServers.gcpMetadata.shutdown()
        MockServers.virusScanner.shutdown()
    }
}

internal fun startOAuth() = with(MockServers.oAuth, MockOAuth2Server::start)
internal fun stopOAuth() = with(MockServers.oAuth, MockOAuth2Server::shutdown)

private fun Application.config() = (environment.config as MapApplicationConfig).apply {
    put("oauth.azure.name", "default")
    put("oauth.azure.discoveryUrl", "${MockServers.oAuth.wellKnownUrl("default")}")
    put("oauth.azure.audience", "default")

    put("oauth.maskinporten.issuer.name", "with-scopes")
    put("oauth.maskinporten.issuer.discoveryUrl", "${MockServers.oAuth.wellKnownUrl("with-scopes")}")
    put("oauth.maskinporten.issuer.audience", "default")
    put("oauth.maskinporten.uploadScope", "nav:helse:helseopplysninger.write")
    put("oauth.maskinporten.downloadScope", "nav:helse:helseopplysninger.read")

    put("fileStore.baseUrl", MockServers.gcs.getBaseUrl())
    put("fileStore.requiresAuth", "true")
    put("fileStore.virusScanningEnabled", "true")
    put(
        "fileStore.tokenFetchUrl",
        "${MockServers.gcpMetadata.getBaseUrl()}/computeMetadata/v1/instance/service-accounts/default/token"
    )
    put("fileStore.virusScannerUrl", "${MockServers.virusScanner.getBaseUrl()}/scan")
}
