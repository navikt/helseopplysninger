package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.client.api.IGenericClient
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.isActive
import no.nav.helse.hops.domain.TaskChange
import no.nav.helse.hops.domain.TaskChangeFeed
import no.nav.helse.hops.fhir.allByQuery
import no.nav.helse.hops.fhir.allByUrl
import no.nav.helse.hops.toIsoString
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime

class FhirHistoryFeedHapi(
    private val fhirClient: IGenericClient
) : TaskChangeFeed {
    override fun poll(since: LocalDateTime): Flow<TaskChange> =
        flow {
            var lastUpdated = since
            while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                val query = createQuery(lastUpdated)
                var last: Task? = null

                fhirClient.allByQuery<Task>(query).forEach {
                    if (!currentCoroutineContext().isActive) return@forEach

                    getHistoryOf(it).fold(null as Task?) { previous, current ->
                        if (lastUpdated < current.meta.lastUpdated.toLocalDateTime())
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
    private inline fun <reified T : Resource> getHistoryOf(resource: T) =
        fhirClient
            .allByUrl("${resource.idElement.toVersionless()}/_history?_at=lt${resource.meta.lastUpdated.toIsoString()}")
            .map { it as T }
            .sortedBy { it.meta.lastUpdated }
            .plus(resource)
            .toList()
}

private fun createQuery(lastUpdated: LocalDateTime): String {
    var query = "_sort=_lastUpdated" // ascending.
    if (lastUpdated > LocalDateTime.MIN)
        query += "&_lastUpdated=gt${lastUpdated.toIsoString()}"

    return query
}
