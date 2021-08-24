package infrastructure

import domain.FileInfo
import domain.VirusScanner
import io.ktor.client.HttpClient
import io.ktor.client.request.put
import io.ktor.features.BadRequestException
import io.ktor.http.ContentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.Serializable

class HttpVirusScanner(
    private val transport: GCPHttpTransport,
    private val httpClient: HttpClient,
    private val config: FileShareConfig.FileStore,
) : VirusScanner {
    private val unScannedBucket = config.unScannedBucketName

    override suspend fun prepareForScan(content: ByteReadChannel, contentType: ContentType, fileName: String): FileInfo =
        transport.upload(unScannedBucket, contentType, content, fileName)

    override suspend fun scan(fileName: String): ByteReadChannel {
        val downloadResponse = transport.download(unScannedBucket, fileName)
        if (!config.virusScanningEnabled) {
            return downloadResponse.content
        }

        val result = httpClient.put<List<ScanResult>>(config.virusScannerUrl) {
            body = downloadResponse.content
        }
        if (result.size != 1) {
            throw RuntimeException("Unexpected result size from virus scan request")
        }
        if (ScanResultStatus.OK != result[0].result) {
            throw FileVirusException(fileName)
        }

        return transport.download(unScannedBucket, fileName).content
    }

    class FileVirusException(fileName: String) : BadRequestException("Malicious content detected in the uploaded file! ref: $fileName")

    @Serializable
    data class ScanResult(val result: ScanResultStatus)

    enum class ScanResultStatus {
        OK,
        FOUND,
        ERROR
    }
}
