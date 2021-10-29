package questionnaire.cache

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.kotlinModule
import com.fasterxml.jackson.module.kotlin.readValue
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import questionnaire.github.GithubApiClient
import java.net.URI

private val log = KotlinLogging.logger {}

object Cache {
    private val cache: MutableSet<QuestionnaireDto> = mutableSetOf()

    fun init(github: GithubApiClient) = runBlocking {
        github.getAllReleaseUrls()
            .map { github.getRelease(it) }
            .map(QuestionnaireDto::create)
            .forEach(::add)
    }

    // TODO: Should this be idempotent by url + version ?
    fun add(dto: QuestionnaireDto) = cache.add(dto)

    fun get(id: String): QuestionnaireDto? = cache.firstOrNull { questionnaire -> questionnaire.id == id }

    fun search(
        uri: URI? = null,
        version: String? = null
    ): List<QuestionnaireDto> = cache
        .filter { schema -> uri?.let { it == schema.url } ?: true }
        .filter { schema -> version?.let { it == schema.version } ?: true }
}

data class QuestionnaireDto(
    private val slice: Slice,
    val raw: String,
) {
    data class Slice(
        val id: String,
        val url: URI,
        val version: String,
    )

    val id: String = slice.id
    val url: URI = slice.url
    val version: String = slice.version

    companion object {
        fun create(content: String): QuestionnaireDto {
            val schemaSlice = mapper.readValue<Slice>(content)
            return QuestionnaireDto(schemaSlice, content)
        }

        private val mapper = ObjectMapper()
            .registerModule(kotlinModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
    }
}
