package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import no.nav.helse.hops.domain.TaskChange
import no.nav.helse.hops.domain.TaskChangeFeed
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import kotlin.coroutines.coroutineContext

class FhirHistoryFeedHapi(
    private val fhirClient: IGenericClient
) : TaskChangeFeed {
    override fun poll(since: LocalDateTime?): Flow<TaskChange> =
        flow {
            var query = createQuery(since)
            while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                var last: Task? = null
                var bundle = fhirClient
                    .search<Bundle>()
                    .byUrl("Task?$query")
                    .execute()

                while (coroutineContext.isActive && bundle?.entry?.isEmpty() == false) {
                    bundle.entry.map { it.resource as Task }.forEach {
                        val prev = previousVersionOrNull(it)
                        emit(TaskChange(it, prev))
                        last = it
                    }
                    bundle = nextPageOrNull(bundle)
                }

                if (last != null) {
                    val lastUpdated = last!!.meta.lastUpdated.toLocalDateTime()
                    query = createQuery(lastUpdated)
                }

                kotlinx.coroutines.delay(5000)
            }
        }

    private fun nextPageOrNull(bundle: Bundle): Bundle? =
        if (bundle.link?.any { it.relation == "next" } == true)
            fhirClient.loadPage().next(bundle).execute()
        else
            null

    private fun <T : Resource> previousVersionOrNull(resource: T): T? {
        val versionId = resource.version()
        if (versionId == 1) return null

        return fhirClient
            .read()
            .resource(resource.javaClass)
            .withIdAndVersion(resource.id, (versionId - 1).toString())
            .execute()
    }
}

/** See https://www.hl7.org/fhir/search.html#date **/
private val fhirDateFormat = DateTimeFormatter.ofPattern("yyyy-mm-ddThh:mm:ss")

private fun createQuery(since: LocalDateTime?): String {
    var query = "_sort=lastUpdated" // ascending.
    if (since != null) query += "&_lastUpdated=gt${since.format(fhirDateFormat)}"

    return query
}

private fun Date.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

private fun IBaseResource.version() = meta.versionId.toInt()
