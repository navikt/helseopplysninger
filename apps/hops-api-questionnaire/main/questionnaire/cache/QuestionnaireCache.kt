package questionnaire.cache

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import questionnaire.github.GithubApiClient
import java.net.URI

object QuestionnaireCache {
    private val cache: MutableSet<Questionnaire> = mutableSetOf()
    private val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun initiate(github: GithubApiClient) = runBlocking {
        val releaseUrls = github.getAllReleaseUrls()
        releaseUrls.forEach { url ->
            val release = github.getRelease(url)
            add(release)
        }
    }

    fun add(rawJson: String) {
        val schemaSlice = mapper.readValue<QuestionnaireSlice>(rawJson)

        // As long as we listen to every release action event type - we must replace potentially updated drafts
        find(schemaSlice.url, schemaSlice.version)?.let(cache::remove)

        cache.add(Questionnaire(schemaSlice, rawJson))
    }

    fun find(id: String): Questionnaire? = cache
        .firstOrNull { questionnaire -> questionnaire.slice.id == id }

    fun find(uri: URI): Questionnaire? = cache
        .firstOrNull { questoinnaire -> questoinnaire.slice.url == uri }

    fun find(uri: URI, version: String): Questionnaire? = cache
        .firstOrNull { questionnaire -> questionnaire.slice.url == uri && questionnaire.slice.version == version }
}

data class Questionnaire(
    /**
     * Selected generic queriable fields from the questionnaire
     */
    val slice: QuestionnaireSlice,

    /**
     * The whole questionnaire schema in raw JSON
     */
    val raw: String,
)

data class QuestionnaireSlice(
    val id: String,
    val url: URI,
    val version: String,
    val title: String,
)
