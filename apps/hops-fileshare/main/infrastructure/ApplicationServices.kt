package infrastructure

import domain.FileSharingService
import io.ktor.client.HttpClient

class ApplicationServices(applicationConfig: FileShareConfig) {
    val fileSharingService: FileSharingService

    init {
        val gcpHttpTransport = GCPHttpTransport(applicationConfig.fileStore)
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
