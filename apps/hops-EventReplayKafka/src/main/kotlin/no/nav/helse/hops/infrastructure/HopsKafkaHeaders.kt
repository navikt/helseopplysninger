package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.api.Constants
import no.nav.helse.hops.domain.FhirMessage
import no.nav.helse.hops.fhir.fullyQualifiedEventType
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders
import org.hl7.fhir.r4.model.MessageHeader
import no.nav.helse.hops.domain.Constants.MessageHeaders

class HopsKafkaHeaders(val kafkaHeaders: Headers) {
    constructor(message: FhirMessage) : this(createRecordHeaders(message))

    val contentType = strValues(Constants.HEADER_CONTENT_TYPE).single()
    val requestId = strValues(Constants.HEADER_REQUEST_ID).single()
    val eventType = strValues(MessageHeaders.EVENT_TYPE).single()
    val offset = strValues(MessageHeaders.OFFSET).single().toLong()
    val source = strValues(MessageHeaders.SOURCE).single()
    val destinations = strValues(MessageHeaders.DESTINATION)

    private fun strValues(key: String) =
        kafkaHeaders.headers(key).map { it.value().decodeToString() }
}

private fun createRecordHeaders(message: FhirMessage) =
    (message.content.entry.first().resource as MessageHeader).let { header ->
        RecordHeaders().apply {
            add(Constants.HEADER_CONTENT_TYPE, message.contentType.toByteArray())
            add(Constants.HEADER_REQUEST_ID, message.requestId.toByteArray())
            add(MessageHeaders.EVENT_TYPE, header.fullyQualifiedEventType.toByteArray())
            add(MessageHeaders.OFFSET, message.sourceOffset.toString().toByteArray())
            add(MessageHeaders.SOURCE, header.source.endpoint.toByteArray())

            header.destination.forEach {
                add(MessageHeaders.DESTINATION, it.endpoint.toByteArray())
            }

            setReadOnly()
        }
    }
