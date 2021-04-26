package no.nav.helse.hops.domain

import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.server.exceptions.ResourceVersionConflictException
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.slf4j.Logger

interface FhirRepository {
    suspend fun addRange(resources: List<Resource>)
}

class FhirRepositoryImpl(
    private val fhirClient: IGenericClient,
    private val logger: Logger
) : FhirRepository {
    override suspend fun addRange(resources: List<Resource>) {
        try {
            val transaction = createTransaction(resources)

            fhirClient
                .transaction()
                .withBundle(transaction)
                .encodedJson()
                .execute()
        } catch (ex: ResourceVersionConflictException) {
            // Provides idempotent-behavior if the same message is send and\or handled multiple times.
            logger.info("Resources have already been created.", ex)
        }
    }
}

private fun createTransaction(resources: List<Resource>) =
    Bundle().apply {
        type = Bundle.BundleType.TRANSACTION
        entry = resources.map {
            Bundle.BundleEntryComponent().apply {
                resource = it
                fullUrl = "urn:uuid:${it.id}"
                request = Bundle.BundleEntryRequestComponent().apply {
                    method = Bundle.HTTPVerb.PUT
                    url = "${it.fhirType()}/${it.id}"
                    ifMatch = "W/\"0\"" // only upsert if resource does not already exist with same ID.
                }
            }
        }
    }
