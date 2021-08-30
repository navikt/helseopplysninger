package fileshare.domain

import io.ktor.client.statement.HttpResponse
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

interface FileStore {
    class DuplicatedFileException(ex: Exception, bucketName: String, fileName: String) : BadRequestException("The file '$fileName' already exists in the bucket '$bucketName'", ex)

    suspend fun save(file: ByteReadChannel, contentType: ContentType, fileName: String): FileInfo
    suspend fun download(fileName: String, range: String?): HttpResponse
    suspend fun findFile(fileName: String): FileInfo?
}
