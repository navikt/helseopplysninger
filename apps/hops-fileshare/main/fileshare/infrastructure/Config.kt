package fileshare.infrastructure

import java.net.URL

data class Config(
    val fileStore: FileStore
) {
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
