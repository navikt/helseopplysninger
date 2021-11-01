package questionnaire.store

import kotlinx.coroutines.runBlocking
import org.hl7.fhir.r4.model.Questionnaire
import questionnaire.fhir.FhirResourceFactory
import questionnaire.fhir.QuestionnaireEnricher
import questionnaire.github.GithubApiClient
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

object QuestionnaireStore {
    private val cache: ConcurrentHashMap<String, Questionnaire> = ConcurrentHashMap(30) // todo: hva er kotlin praksis?

    // TODO: flytt ut i egen service
    fun init(github: GithubApiClient) = runBlocking {
        github.getAllReleaseUrls()
            .map { github.getRelease(it) } // TODO: parallelliser kallet
            .map(FhirResourceFactory::questionnaire)
            .map { QuestionnaireEnricher.enrich(timestamp, it) }
            .forEach(::add)
    }

    fun add(questionnaire: Questionnaire) = cache.put(questionnaire.id, questionnaire)

    fun get(id: String): Questionnaire? =
        cache.filterValues { questionnaire -> questionnaire.id == id }.values.singleOrNull()

    fun search(
        uri: URI? = null,
        version: String? = null
    ): Collection<Questionnaire> = cache
        .filterValues { schema -> uri?.let { it.toString() == schema.url } ?: true } // todo: test it.tostring
        .filterValues { schema -> version?.let { it == schema.version } ?: true }
        .values
}
