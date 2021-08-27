package fileshare.infrastructure

import fileshare.domain.FileInfo
import fileshare.domain.FileStore
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel

class GCPHttpFileStore(
    private val transport: GCPHttpTransport,
    private val config: Config.FileStore
) : FileStore {

    override suspend fun save(file: ByteReadChannel, contentType: ContentType, fileName: String): FileInfo =
        transport.upload(config.bucketName, contentType, file, fileName)

    override suspend fun download(fileName: String, range: String?): HttpResponse =
        transport.download(config.bucketName, fileName, range)
}
