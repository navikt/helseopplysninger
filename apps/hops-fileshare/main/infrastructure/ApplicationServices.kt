package infrastructure

import domain.StorageClient
import io.ktor.client.HttpClient

class ApplicationServices(applicationConfig: Configuration) {
    val storageClient: StorageClient

    init {
        val httpClient = HttpClient()
        // TODO setup Auth
        storageClient = GCPHttpStorageClient(
            httpClient,
            applicationConfig.fileStoreConfig
        )
    }
}
