package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import no.nav.helse.hops.domain.TaskChange
import no.nav.helse.hops.domain.TaskChangeFeed
import no.nav.helse.hops.domain.allByQuery
import no.nav.helse.hops.domain.allByUrl
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date

class FhirHistoryFeedHapi(
    private val fhirClient: IGenericClient
) : TaskChangeFeed {
    override fun poll(since: LocalDateTime?): Flow<TaskChange> =
        flow {
            var lastUpdated = since
            while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                val query = createQuery(lastUpdated)
                var last: Task? = null

                fhirClient.allByQuery<Task>(query).forEach {
                    if (!currentCoroutineContext().isActive) return@forEach

                    history(it).fold(null as Task?) { previous, current ->
                        if (current.lastModified.toLocalDateTime() > lastUpdated)
                            emit(TaskChange(current, previous))
                        current
                    }

                    last = it
                }

                if (last != null)
                    lastUpdated = last!!.meta.lastUpdated.toLocalDateTime()

                kotlinx.coroutines.delay(5000)
            }
        }

    /** Returns a list of all the versions of a resource, ordered from first to (inclusive) the supplied version. **/
    private inline fun <reified T : Resource> history(resource: T) =
        fhirClient
            .allByUrl("${resource.fhirType()}/${resource.id}/_history?_at=lt${resource.meta.lastUpdated.toFhirString()}")
            .map { it as T }
            .sortedBy { it.meta.lastUpdated }
            .plus(resource)
            .toList()
}

private fun createQuery(lastUpdated: LocalDateTime?): String {
    var query = "_sort=_lastUpdated" // ascending.
    if (lastUpdated != null)
        query += "&_lastUpdated=gt${lastUpdated.format(DateTimeFormatter.ISO_INSTANT)}"

    return query
}

private fun Date.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
private fun Date.toFhirString(): String = toLocalDateTime().format(DateTimeFormatter.ISO_INSTANT)
