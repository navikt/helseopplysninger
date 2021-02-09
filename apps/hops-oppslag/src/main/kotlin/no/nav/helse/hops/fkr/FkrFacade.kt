package no.nav.helse.hops.fkr

import io.ktor.client.*
import io.ktor.client.request.*
import ca.uhn.fhir.context.FhirContext
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Practitioner

interface FkrFacade {
    suspend fun practitionerName(hprNr: Int): String
}

class FkrFacadeImpl(
    private val httpClient: HttpClient,
    private val fhirCtx: FhirContext
    ) : FkrFacade {
    override suspend fun practitionerName(hprNr: Int): String {
        val json = httpClient.get<String>("/Practitioner?identifier=urn:oid:2.16.578.1.12.4.1.4.4|$hprNr")

        val parser = fhirCtx.newJsonParser()
        val bundle = parser.parseResource(Bundle::class.java, json)
        val practitioner = bundle.entry.firstOrNull()?.resource as Practitioner?
        val name = practitioner?.name?.firstOrNull()?.family ?: ""

        return name
    }
}