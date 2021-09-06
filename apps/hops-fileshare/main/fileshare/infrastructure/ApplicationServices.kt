package fileshare.infrastructure

import fileshare.domain.FileSharingService
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import kotlinx.serialization.json.Json

class ApplicationServices(
    applicationConfig: Config,
) {
    val fileSharingService: FileSharingService

    private val baseHttpClient = HttpClient() {
        install(JsonFeature) {
            serializer = KotlinxSerializer(Json { ignoreUnknownKeys = true })
        }
    }

    init {
        val config = applicationConfig.fileStore
        val gcpHttpTransport = GCPHttpTransport(baseHttpClient, config)
        val virusScanner = HttpVirusScanner(
            gcpHttpTransport,
            baseHttpClient,
            config
        )
        val fileStore = GCPHttpFileStore(
            gcpHttpTransport,
            config
        )
        fileSharingService = FileSharingService(virusScanner, fileStore)
    }
}
