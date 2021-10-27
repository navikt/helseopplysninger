package api.questionnaire.github

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL

object QuestionnaireCache {
    private val cache: MutableSet<QuestionnaireEntry> = mutableSetOf()
    private val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun add(rawSchema: String) {
        val schemaSlice = mapper.readValue<QuestionnaireSlice>(rawSchema)
        val cacheEntry = QuestionnaireEntry(schemaSlice, rawSchema)

        val maybeExisting = get(schemaSlice.url, schemaSlice.version)
        if (maybeExisting != null) cache.remove(maybeExisting)

        cache.add(cacheEntry)
    }

    fun get(id: String) = cache.firstOrNull { it.schema.id == id }
    fun get(url: URL) = cache.firstOrNull { it.schema.url == url }
    fun get(url: URL, version: String) = cache.firstOrNull { it.schema.url == url && it.schema.version == version }
}

data class QuestionnaireEntry(
    /**
     * Selected fields from the schema
     */
    val schema: QuestionnaireSlice,

    /**
     * The raw schema
     */
    val raw: String,
)

/**
 * Selected fields from the questionnaire schema used for filtering and searching purpose
 */
data class QuestionnaireSlice(
    val id: String,
    val url: URL,
    val version: String,
    val title: String,
)
