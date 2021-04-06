package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.OperationOutcome
import org.slf4j.Logger

class FhirMessageProcessorImpl(
    private val logger: Logger
) : FhirMessageProcessor {
    override suspend fun process(message: Bundle): OperationOutcome {
        // TODO: Publish resources to the HAPI fhir server
        logger.info("Message: ${message.toJson()}")
        return OperationOutcome()
    }
}
