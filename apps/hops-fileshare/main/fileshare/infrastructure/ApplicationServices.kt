package fileshare.infrastructure

import fileshare.domain.FileSharingService
import io.ktor.client.HttpClient
import io.ktor.config.ApplicationConfig
import java.net.URL

class ApplicationServices(
    applicationConfig: ApplicationConfig,
) {
    val fileSharingService: FileSharingService

    private val baseHttpClient = HttpClient()

    init {
        val config = Config.FileStore(
            URL(applicationConfig.property("fileStore.baseUrl").getString()),
            applicationConfig.property("fileStore.bucketName").getString(),
            applicationConfig.property("fileStore.requiresAuth").getString().toBooleanStrict(),
            URL(applicationConfig.property("fileStore.tokenFetchUrl").getString()),
            applicationConfig.property("fileStore.virusScanningEnabled").getString().toBooleanStrict(),
            URL(applicationConfig.property("fileStore.virusScannerUrl").getString()),
            applicationConfig.property("fileStore.unScannedBucketName").getString(),
        )
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
