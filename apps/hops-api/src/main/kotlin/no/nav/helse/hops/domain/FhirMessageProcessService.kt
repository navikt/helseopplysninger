package no.nav.helse.hops.domain

import no.nav.helse.hops.IdentityGenerator
import no.nav.helse.hops.fhir.client.FhirClient
import no.nav.helse.hops.fhir.client.read
import no.nav.helse.hops.fhir.client.readOrNull
import no.nav.helse.hops.fhir.idAsUUID
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.fhir.resources
import org.hl7.fhir.r4.model.MessageHeader
import org.hl7.fhir.r4.model.Resource

class FhirMessageProcessService(private val fhirClient: FhirClient) {
    suspend fun process(message: GenericMessage): OkResponseMessage {
        val requestId = message.header.idAsUUID()
        var header = fhirClient.readOrNull<MessageHeader>(requestId)

        if (header == null) { // determines if message has already been received.
            val resources = message.bundle.resources<Resource>()
            fhirClient.upsertAsTransaction(resources)
            header = fhirClient.read(requestId)
        }

        val responseId = IdentityGenerator.createUUID5(requestId, "response")
        return OkResponseMessage(header, responseId)
    }
}
