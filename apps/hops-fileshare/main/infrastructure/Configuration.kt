package infrastructure

import java.net.URL

data class Configuration(
    val fileStoreConfig: FileStoreConfig
)

data class FileStoreConfig(
    val baseUrl: URL,
    val bucketName: String
)
