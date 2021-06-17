package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.api.Constants
import no.nav.helse.hops.domain.Constants.MessageHeaders
import no.nav.helse.hops.domain.FhirMessage
import org.apache.kafka.common.header.Headers
import org.apache.kafka.common.header.internals.RecordHeaders

class HopsKafkaHeaders(val kafkaHeaders: Headers) {
    constructor(message: FhirMessage) : this(createRecordHeaders(message))

    val contentType = strValues(Constants.HEADER_CONTENT_TYPE).single()
    val requestId = strValues(Constants.HEADER_REQUEST_ID).single()
    val sourceOffset = strValues(MessageHeaders.SOURCE_OFFSET).single().toLong()

    private fun strValues(key: String) =
        kafkaHeaders.headers(key).map { it.value().decodeToString() }
}

private fun createRecordHeaders(message: FhirMessage) =
    RecordHeaders().apply {
        add(Constants.HEADER_CONTENT_TYPE, message.contentType.toByteArray())
        add(Constants.HEADER_REQUEST_ID, message.requestId.toByteArray())
        add(MessageHeaders.SOURCE_OFFSET, message.sourceOffset.toString().toByteArray())

        setReadOnly()
    }
