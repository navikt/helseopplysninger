package fileshare.infrastructure

import java.net.URL

data class Config(
    val fileStore: FileStore
) {
    data class FileStore(
        val baseUrl: URL,
        val bucketName: String,
        val requireAuth: Boolean,
        val tokenFetchUrl: URL
    )
}
