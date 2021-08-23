package fileshare.infrastructure

import fileshare.domain.StorageClient

class ApplicationServices(applicationConfig: Config) {
    val storageClient: StorageClient

    init {
        storageClient = GCPHttpStorageClient(
            GCPHttpTransport(applicationConfig.fileStore).httpClient,
            applicationConfig.fileStore
        )
    }
}
