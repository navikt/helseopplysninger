package fileshare.infrastructure

import java.net.URL
import no.nav.helse.hops.hoplite.OauthIssuerConfig

data class Config(
    val oauth: ModuleOAuth,
    val fileStore: FileStore
) {
    data class ModuleOAuth(
        val azure: OauthIssuerConfig,
        val maskinporten: OauthIssuerWithScope
    )
    data class OauthIssuerWithScope(
        val issuer: OauthIssuerConfig,
        val uploadScope: String,
        val downloadScope: String
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
