package infrastructure

import domain.StorageClient
import io.ktor.client.HttpClient

class ApplicationServices(applicationConfig: FileShareConfig) {
    val storageClient: StorageClient

    init {
        val gcpHttpTransport = GCPHttpTransport(applicationConfig.fileStore)
        val virusScanner = VirusScanner(
            gcpHttpTransport,
            HttpClient(),
            applicationConfig.fileStore
        )
        storageClient = GCPHttpStorageClient(
            gcpHttpTransport,
            applicationConfig.fileStore,
            virusScanner
        )
    }
}
