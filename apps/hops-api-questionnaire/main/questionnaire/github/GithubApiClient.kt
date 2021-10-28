package questionnaire.github

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get

class GithubApiClient(private val config: GithubConfig) {

    private val client: HttpClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer() {
                // Allow mapping to selected fields from the JSON-structures
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    suspend fun getAllReleaseUrls(): List<String> {
        val result = client.get<Releases>(config.github.questionnaireUrl)
        return result.releases
            .flatMap(Release::assets)
            .map(Asset::browser_download_url)
    }

    suspend fun getRelease(downloadUrl: String): String = client.get(downloadUrl)

    private data class Releases(val releases: List<Release>)
    private data class Release(val prerelease: Boolean, val assets: List<Asset>)
    private data class Asset(val browser_download_url: String)
}
