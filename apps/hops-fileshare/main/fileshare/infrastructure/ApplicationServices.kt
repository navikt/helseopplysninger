package fileshare.infrastructure

import fileshare.domain.FileSharingService
import io.ktor.client.HttpClient

class ApplicationServices(
    httpClient: HttpClient,
    applicationConfig: Config,
) {
    val fileSharingService: FileSharingService

    init {
        val gcpHttpTransport = GCPHttpTransport(httpClient, applicationConfig.fileStore)
        val virusScanner = HttpVirusScanner(
            gcpHttpTransport,
            httpClient,
            applicationConfig.fileStore
        )
        val fileStore = GCPHttpFileStore(
            gcpHttpTransport,
            applicationConfig.fileStore
        )
        fileSharingService = FileSharingService(virusScanner, fileStore)
    }
}
