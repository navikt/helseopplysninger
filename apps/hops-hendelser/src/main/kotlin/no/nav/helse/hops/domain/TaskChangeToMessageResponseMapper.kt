package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import org.hl7.fhir.instance.model.api.IIdType
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Reference
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.UUID

class TaskChangeToMessageResponseMapper(
    private val fhirClient: IGenericClient
) : Mapper<TaskChange, Bundle> {
    override suspend fun map(input: TaskChange) = createMessageResponse(input.current)

    private fun createMessageResponse(task: Task): Bundle {
        val requestMessageHeader = fhirClient
            .allByQuery<MessageHeader>("focus=${task.id}")
            .single()

        val instant = task.meta.lastUpdated.toLocalDateTime()
        val focusResources = requestMessageHeader.focus.map {
            resourceAtInstant(it.referenceElement, instant)
        }

        val responseMessageHeader = MessageHeader().apply {
            id = IdentityGenerator.CreateUUID5(task.id, task.meta.versionId).toString()
            event = requestMessageHeader.event
            destination = listOf(asDestination(requestMessageHeader.source))
            source = asSource(requestMessageHeader.destination.single())
            response = MessageHeader.MessageHeaderResponseComponent().apply {
                identifier = requestMessageHeader.id
                code = MessageHeader.ResponseType.OK
            }
            focus = listOf(Reference(task)) + focusResources.map { Reference(it) }
        }

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.MESSAGE
            addResource(responseMessageHeader)
            addResource(task)
            focusResources.forEach { addResource(it) }
        }
    }

    private fun resourceAtInstant(id: IIdType, instant: LocalDateTime) =
        fhirClient
            .allByUrl("${id.value}/_history?_count=1&_at=le${instant.format(fhirDateFormat)}")
            .single()
}

private val fhirDateFormat = DateTimeFormatter.ofPattern("yyyy-mm-ddThh:mm:ss")
private fun Date.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())

private fun asDestination(src: MessageHeader.MessageSourceComponent) =
    MessageHeader.MessageDestinationComponent(src.endpointElement).apply { name = src.name }

private fun asSource(dest: MessageHeader.MessageDestinationComponent) =
    MessageHeader.MessageSourceComponent(dest.endpointElement).apply { name = dest.name }
