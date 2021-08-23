package infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import java.util.UUID

class VirusScanner(
    private val transport: GCPHttpTransport,
    private val httpClient: HttpClient,
    private val config: FileShareConfig.FileStore,
) {
    private val unScannedBucket = config.unScannedBucketName

    suspend fun scan(file: ByteReadChannel, contentType: ContentType): ByteReadChannel {
        if (!config.virusScanningEnabled) {
            return file
        }
        val tempFileName = UUID.randomUUID().toString()
        transport.upload(unScannedBucket, contentType, file, tempFileName)
        val download = transport.download(unScannedBucket, tempFileName)
        httpClient.put<HttpResponse>(config.virusScannerUrl) {
            body = download.content
        }

        return transport.download(unScannedBucket, tempFileName).content
    }
}
