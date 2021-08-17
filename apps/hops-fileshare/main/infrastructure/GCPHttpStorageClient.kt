package infrastructure

import domain.StorageClient
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import java.util.UUID

class GCPHttpStorageClient(
    private val httpClient: HttpClient,
    private val gcpConfig: FileStoreConfig
) : StorageClient {

    override suspend fun save(file: ByteReadChannel, contentType: ContentType): String {
        val fileName = UUID.randomUUID().toString()
        httpClient.post<HttpResponse>("${gcpConfig.baseUrl}/upload/storage/v1/b/${gcpConfig.bucketName}/o?uploadType=media&name=$fileName") {
            this.body = file
            contentType(contentType)
        }

        return fileName
    }

    override suspend fun download(fileName: String): HttpResponse {
        return httpClient.get("${gcpConfig.baseUrl}/storage/v1/b/${gcpConfig.bucketName}/o/${fileName}?alt=media")
    }
}
