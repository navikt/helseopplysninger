package infrastructure

import domain.StorageClient

class ApplicationServices(applicationConfig: FileShareConfig) {
    val storageClient: StorageClient

    init {
        storageClient = GCPHttpStorageClient(
            GCPHttpTransport(applicationConfig.fileStore).httpClient,
            applicationConfig.fileStore
        )
    }
}
