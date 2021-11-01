package questionnaire.store

import org.hl7.fhir.r4.model.Questionnaire
import java.net.URI
import java.util.concurrent.ConcurrentHashMap

object QuestionnaireStore {
    private val store: ConcurrentHashMap<String, Questionnaire> = ConcurrentHashMap()

    fun add(questionnaire: Questionnaire) = when (store.containsKey(questionnaire.id)) {
        true -> store.replace(questionnaire.id, questionnaire)
        false -> store.put(questionnaire.id, questionnaire)
    }

    fun get(id: String): Questionnaire? = store
        .filterValues { it.id == id }
        .values
        .singleOrNull()

    fun search(
        uri: URI? = null,
        version: String? = null
    ): Collection<Questionnaire> = store
        .filterValues { schema -> uri?.let { it.toString() == schema.url } ?: true } // todo: test it.tostring
        .filterValues { schema -> version?.let { it == schema.version } ?: true }
        .values
}
