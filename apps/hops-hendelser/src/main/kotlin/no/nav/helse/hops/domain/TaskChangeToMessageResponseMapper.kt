package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.single
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.Mapper
import no.nav.helse.hops.fhir.client.FhirClientReadOnly
import no.nav.helse.hops.fhir.client.search
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.toIsoString
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.ResourceType
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime
import java.util.UUID

class TaskChangeToMessageResponseMapper(
    private val fhirClient: FhirClientReadOnly
) : Mapper<TaskChange, OkResponseMessage?> {
    override suspend fun map(input: TaskChange) = createMessageResponse(input.current)

    private suspend fun createMessageResponse(task: Task): OkResponseMessage? {
        val requestMessageHeader = fhirClient
            .search<MessageHeader>("focus=${task.idElement.idPart}")
            .firstOrNull() ?: return null // Returns NULL if the Task is not associated with a Request-Message.

        val instant = task.meta.lastUpdated.toLocalDateTime()
        val focusResources = requestMessageHeader.focus.map {
            resourceAtInstant(it.referenceElement, instant)
        }

        val responseId = IdentityGenerator.createUUID5(task.idAsUUID(), task.meta.versionId)
        return OkResponseMessage(requestMessageHeader, responseId, focusResources)
    }

    private suspend fun resourceAtInstant(id: IIdType, instant: LocalDateTime) =
        fhirClient
            .history(
                ResourceType.fromCode(id.resourceType),
                UUID.fromString(id.idPart),
                "_count=1&_at=le${instant.toIsoString()}"
            )
            .single()
}
