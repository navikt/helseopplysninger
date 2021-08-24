package domain

import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

interface FileStore {
    suspend fun save(file: ByteReadChannel, contentType: ContentType, fileName: String): FileInfo
    suspend fun download(fileName: String, range: String?): HttpResponse
}
