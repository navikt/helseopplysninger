package domain

import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

interface StorageClient {
    suspend fun save(file: ByteReadChannel, contentType: ContentType): String
}
