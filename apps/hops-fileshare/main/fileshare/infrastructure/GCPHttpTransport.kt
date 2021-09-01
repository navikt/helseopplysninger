package fileshare.infrastructure

import fileshare.domain.FileInfo
import fileshare.domain.FileStore
import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BearerTokens
import io.ktor.client.features.auth.providers.bearer
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.Parameters
import io.ktor.http.contentType
import io.ktor.http.formUrlEncode
import io.ktor.utils.io.ByteReadChannel
import java.util.Base64
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

class GCPHttpTransport(private val baseHttpClient: HttpClient, private val config: Config.FileStore) {
    private val httpClient: HttpClient

    init {
        httpClient = baseHttpClient.config {
            if (config.requiresAuth) {
                install(Auth) {
                    bearer {
                        loadTokens { fetchToken(baseHttpClient) }
                        refreshTokens { fetchToken(baseHttpClient) }
                    }
                }
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

    suspend fun upload(bucketName: String, contentType: ContentType, scannedFile: ByteReadChannel, fileName: String): FileInfo {
        val params = Parameters.build {
            append("uploadType", "media")
            append("name", fileName)
            append("ifGenerationMatch", "0")
        }.formUrlEncode()

        try {
            val fileInfo = httpClient.post<FileInfo>("${config.baseUrl}/upload/storage/v1/b/$bucketName/o?$params") {
                body = scannedFile
                contentType(contentType)
            }

            fun ByteArray.toHex() = joinToString(separator = "") { byte -> "%02x".format(byte) }
            return fileInfo.copy(
                md5Hash = Base64.getDecoder().decode(fileInfo.md5Hash).toHex()
            )
        } catch (ex: ClientRequestException) {
            if (ex.response.status == HttpStatusCode.PreconditionFailed) {
                throw FileStore.DuplicatedFileException(ex, bucketName, fileName)
            }
            throw ex
        }
    }

    suspend fun download(bucketName: String, fileName: String, range: String? = null): HttpResponse =
        httpClient.get("${config.baseUrl}/storage/v1/b/$bucketName/o/$fileName?alt=media") {
            if (range != null) {
                headers {
                    append(HttpHeaders.Range, range)
                }
            }
        }

    suspend fun findFile(bucketName: String, fileName: String): FileInfo? {
        try {
            return httpClient.get("${config.baseUrl}/storage/v1/b/$bucketName/o/$fileName?alt=json")
        } catch (ex: ClientRequestException) {
            if (ex.response.status == HttpStatusCode.NotFound) {
                return null
            }
            throw ex
        }
    }
}

@Serializable
private data class TokenInfo(
    @SerialName("access_token") val accessToken: String,
    @SerialName("expires_in") val expiresIn: Int,
    @SerialName("token_type") val tokenType: String
)
