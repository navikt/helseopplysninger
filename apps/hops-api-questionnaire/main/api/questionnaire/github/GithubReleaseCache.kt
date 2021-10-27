package api.questionnaire.github

import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import java.net.URL

object GithubReleaseCache {
    private val cache: MutableSet<CacheEntry> = mutableSetOf()
    private val mapper = ObjectMapper().configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)

    fun add(release: String) = cache.add(mapper.readValue<CacheEntry>(release).copy(schema = release))

    fun get(id: String): String? = cache.firstOrNull { it.id == id }?.schema
    fun get(url: URL) = cache.firstOrNull { it.url == url }
    fun get(url: URL, version: String) = cache.firstOrNull { it.url == url && it.version == version }
}

/**
 * @param schema is the whole document
 */
data class CacheEntry(
    val id: String,
    val url: URL,
    val version: String,
    val resourceType: String,
    val title: String,
    val status: String,
    val schema: String?,
)
