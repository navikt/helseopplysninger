package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.toList
import no.nav.helse.hops.domain.TaskChange
import no.nav.helse.hops.domain.TaskChangeFeed
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.fhir.client.history
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.toIsoString
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.r4.model.Resource
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime

class FhirHistoryFeedHapi(
    private val fhirClient: FhirClientReadOnly
) : TaskChangeFeed {
    override fun poll(since: LocalDateTime): Flow<TaskChange> =
        flow {
            var lastUpdated = since
            while (true) { // Will be exited when the flow's CoroutineContext is cancelled.
                val query = createQuery(lastUpdated)
                var last: Task? = null

                fhirClient.search<Task>(query).collect {
                    // Pull the whole history of the Task, in case there are older changes
                    // that are within the requested timespan.
                    getHistoryOf(it).fold(null as Task?) { previous, current ->
                        if (lastUpdated < current.meta.lastUpdated.toLocalDateTime())
                            emit(TaskChange(current, previous))
                        current
                    }

                    last = it
                }

                if (last != null)
                    lastUpdated = last!!.meta.lastUpdated.toLocalDateTime()

                // TODO: The polling-interval should be dynamically adjusted according to activity level.
                kotlinx.coroutines.delay(5000)
            }
        }

    /** Returns a list of all the versions of a resource, ordered from first to (inclusive) the supplied version. **/
    private suspend inline fun <reified T : Resource> getHistoryOf(resource: T) =
        fhirClient
            .history<T>(resource.idAsUUID(), "_at=lt${resource.meta.lastUpdated.toIsoString()}")
            .toList()
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
