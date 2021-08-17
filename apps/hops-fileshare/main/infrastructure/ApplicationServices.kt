package infrastructure

import domain.StorageClient
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BearerTokens
import io.ktor.client.features.auth.providers.bearer

class ApplicationServices(applicationConfig: Configuration) {
    val storageClient: StorageClient

    init {
        storageClient = GCPHttpStorageClient(
            GCPHttpTransport(applicationConfig.fileStoreConfig).httpClient,
            applicationConfig.fileStoreConfig
        )
    }
}
