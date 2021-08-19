package infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BearerTokens
import io.ktor.client.features.auth.providers.bearer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.features.json.serializer.KotlinxSerializer
import io.ktor.client.request.get
import io.ktor.client.request.headers
import kotlinx.serialization.Serializable
import kotlinx.serialization.SerialName

class GCPHttpTransport(private val gcsConfig: FileStoreConfig) {
    val httpClient: HttpClient

    init {
        httpClient = HttpClient() {
            if (gcsConfig.requireAuth) {
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
        val tokenInfo = httpClient.get<TokenInfo>(gcsConfig.tokenFetchUrl) {
            headers {
                append("Metadata-Flavor", "Google")
            }
        }

        return BearerTokens(
            accessToken = tokenInfo.accessToken,
            refreshToken = ""
        )
    }
}

@Serializable
private data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("token_type") val tokenType: String
)
