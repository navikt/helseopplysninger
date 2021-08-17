package infrastructure

import domain.StorageClient

class ApplicationServices(applicationConfig: Configuration) {
    val storageClient: StorageClient

    init {
        storageClient = GCPHttpStorageClient(
            GCPHttpTransport(applicationConfig.fileStoreConfig).httpClient,
            applicationConfig.fileStoreConfig
        )
    }
}
