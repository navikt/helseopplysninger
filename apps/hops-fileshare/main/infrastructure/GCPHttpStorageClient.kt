package infrastructure

import domain.StorageClient
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import java.util.UUID

class GCPHttpStorageClient(
    private val transport: GCPHttpTransport,
    private val config: FileShareConfig.FileStore,
    private val virusScanner: VirusScanner
) : StorageClient {

    override suspend fun save(file: ByteReadChannel, contentType: ContentType): String {
        val scannedFile = virusScanner.scan(file, contentType)

        val fileName = UUID.randomUUID().toString()
        transport.upload(config.bucketName, contentType, scannedFile, fileName)
        return fileName
    }

    override suspend fun download(fileName: String): HttpResponse {
        return transport.download(config.bucketName, fileName)
    }
}
