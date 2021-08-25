package fileshare.domain

import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

interface StorageClient {
    suspend fun save(file: ByteReadChannel, contentType: ContentType): String
    suspend fun download(fileName: String): HttpResponse
}
