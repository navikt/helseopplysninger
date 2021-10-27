package api.questionnaire.github

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

    suspend fun getAllReleases(): List<String> {
        val result = client.get<Release.Releases>(config.github.questionnaireUrl)
        return result.releases
            .flatMap(Release.Release::assets)
            .map(Release.Asset::browser_download_url)
    }
}
