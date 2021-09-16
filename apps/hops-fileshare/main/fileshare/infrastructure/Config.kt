package fileshare.infrastructure

import java.net.URL
import no.nav.helse.hops.security.HopsAuth

data class Config(
    val oauth: ModuleOAuth,
    val fileStore: FileStore
) {
    data class ModuleOAuth(
        val azure: HopsAuth.Configuration.IssuerConfig,
        val maskinporten: HopsAuth.Configuration.Maskinporten
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
