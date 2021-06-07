package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.firstOrNull
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.Mapper
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.fhir.client.pullResourceGraphSnapshot
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Task

class TaskChangeToMessageResponseMapper(
    private val fhirClient: FhirClientReadOnly
) : Mapper<TaskChange, OkResponseMessage?> {
    override suspend fun map(input: TaskChange) = createMessageResponse(input.current)

    private suspend fun createMessageResponse(task: Task): OkResponseMessage? {
        val requestMessageHeader = fhirClient
            .search<MessageHeader>("focus=${task.idElement.idPart}")
            .firstOrNull() ?: return null // Returns NULL if the Task is not associated with a Request-Message.

        val responseId = IdentityGenerator.createUUID5(task.idAsUUID(), task.meta.versionId)
        val data = fhirClient.pullResourceGraphSnapshot(task)

        return OkResponseMessage(requestMessageHeader, responseId, data)
    }
}
