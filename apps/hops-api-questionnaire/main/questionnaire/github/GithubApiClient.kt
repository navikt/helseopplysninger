package questionnaire.github

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader

class GithubApiClient(private val config: GithubConfig) {
    private val client: HttpClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    suspend fun getAllReleaseUrls(): List<String> =
        client.get<List<Release>>(config.questionnaireUrl)
            .flatMap(Release::assets)
            .map(Asset::browser_download_url)

    suspend fun getRelease(downloadUrl: String): String =
        client.get<HttpStatement>(downloadUrl).execute { response ->
            val channel: ByteReadChannel = response.receive()
            withContext(Dispatchers.IO) {
                channel.toInputStream()
                    .bufferedReader()
                    .use(BufferedReader::readText)
            }
        }

    private data class Release(val prerelease: Boolean, val assets: List<Asset>)
    private data class Asset(val browser_download_url: String)
}
