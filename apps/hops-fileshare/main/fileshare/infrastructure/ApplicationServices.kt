package fileshare.infrastructure

import fileshare.domain.FileSharingService
import io.ktor.client.HttpClient

class ApplicationServices(applicationConfig: Config) {
    val fileSharingService: FileSharingService

    init {
        val gcpHttpTransport = GCPHttpTransport(HttpClient(), applicationConfig.fileStore)
        val virusScanner = HttpVirusScanner(
            gcpHttpTransport,
            HttpClient(),
            applicationConfig.fileStore
        )
        val fileStore = GCPHttpFileStore(
            gcpHttpTransport,
            applicationConfig.fileStore
        )
        fileSharingService = FileSharingService(virusScanner, fileStore)
    }
}
