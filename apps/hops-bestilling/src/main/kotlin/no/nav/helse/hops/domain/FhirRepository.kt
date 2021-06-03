package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException
import no.nav.helse.hops.fhir.client.FhirClient
import org.hl7.fhir.r4.model.Resource
import org.slf4j.Logger

interface FhirRepository {
    suspend fun addRange(resources: List<Resource>)
}

class FhirRepositoryImpl(
    private val fhirClient: FhirClient,
    private val logger: Logger
) : FhirRepository {
    override suspend fun addRange(resources: List<Resource>) {
        try {
            fhirClient.upsertAsTransaction(resources)
        } catch (ex: ResourceVersionConflictException) {
            // Provides idempotent-behavior if the same message is send and\or handled multiple times.
            logger.info("Resources have already been created.", ex)
        }
    }
}
