package infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BearerTokens
import io.ktor.client.features.auth.providers.bearer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.utils.io.ByteReadChannel
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GCPHttpTransport(private val config: FileShareConfig.FileStore) {
    val httpClient: HttpClient

    init {
        httpClient = HttpClient() {
            if (config.requiresAuth) {
                val tokenClient = HttpClient() {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer()
                    }
                }
                install(Auth) {
                    bearer {
                        loadTokens { fetchToken(tokenClient) }
                        refreshTokens { fetchToken(tokenClient) }
                    }
                }
            }

            install(JsonFeature) {
                serializer = KotlinxSerializer()
            }
        }
    }

    private suspend fun fetchToken(httpClient: HttpClient): BearerTokens {
        /*
        Vi får token av metadata server som kjøres sammen med poden, den token er knyttet til
        workload identity:
        https://cloud.google.com/kubernetes-engine/docs/how-to/workload-identity#gke_mds
         */
        val tokenInfo = httpClient.get<TokenInfo>(config.tokenFetchUrl) {
            headers {
                append("Metadata-Flavor", "Google")
            }
        }

        return BearerTokens(
            accessToken = tokenInfo.accessToken,
            refreshToken = ""
        )
    }

    suspend fun upload(bucketName: String, contentType: ContentType, scannedFile: ByteReadChannel, fileName: String): HttpResponse {
        return httpClient.post("${config.baseUrl}/upload/storage/v1/b/$bucketName/o?uploadType=media&name=$fileName") {
            body = scannedFile
            contentType(contentType)
        }
    }

    suspend fun download(bucketName: String, fileName: String): HttpResponse {
        return httpClient.get("${config.baseUrl}/storage/v1/b/$bucketName/o/$fileName?alt=media")
    }
}

@Serializable
private data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("token_type") val tokenType: String
)
