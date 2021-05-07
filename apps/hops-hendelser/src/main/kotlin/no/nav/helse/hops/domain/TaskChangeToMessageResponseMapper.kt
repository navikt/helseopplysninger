package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.Mapper
import no.nav.helse.hops.fhir.allByQuery
import no.nav.helse.hops.fhir.allByUrl
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.toIsoString
import no.nav.helse.hops.toLocalDateTime
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime

class TaskChangeToMessageResponseMapper(
    private val fhirClient: IGenericClient
) : Mapper<TaskChange, OkResponseMessage> {
    override suspend fun map(input: TaskChange) = createMessageResponse(input.current)

    private fun createMessageResponse(task: Task): OkResponseMessage {
        val requestMessageHeader = fhirClient
            .allByQuery<MessageHeader>("focus=${task.idElement.idPart}")
            .single()

        val instant = task.meta.lastUpdated.toLocalDateTime()
        val focusResources = requestMessageHeader.focus.map {
            resourceAtInstant(it.referenceElement, instant)
        }

        val responseId = IdentityGenerator.createUUID5(task.idElement.idPart, task.meta.versionId)

        return OkResponseMessage(requestMessageHeader, responseId, focusResources)
    }

    private fun resourceAtInstant(id: IIdType, instant: LocalDateTime) =
        fhirClient
            .allByUrl("${id.toVersionless()}/_history?_count=1&_at=le${instant.toIsoString()}")
            .single()
}