package fileshare.infrastructure

import no.nav.helse.hops.security.IssuerConfig
import no.nav.helse.hops.security.MaskinportenProvider
import java.net.URL

data class Config(
    val oauth: ModuleOAuth,
    val fileStore: FileStore
) {
    data class ModuleOAuth(
        val azure: IssuerConfig,
        val maskinporten: MaskinportenProvider.Configuration
    )
    data class FileStore(
        val baseUrl: URL,
        val bucketName: String,
        val requiresAuth: Boolean,
        val tokenFetchUrl: URL,
        val virusScanningEnabled: Boolean,
        val virusScannerUrl: URL,
        val unScannedBucketName: String
    )
}
