package infrastructure

import java.net.URL

data class FileShareConfig(
    val fileStore: FileStore
) {
    data class FileStore(
        val baseUrl: URL,
        val bucketName: String,
        val requireAuth: Boolean,
        val tokenFetchUrl: URL
    )
}
