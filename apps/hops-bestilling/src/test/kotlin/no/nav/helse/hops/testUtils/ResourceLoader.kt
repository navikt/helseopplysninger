package no.nav.helse.hops.testUtils

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.hl7.fhir.instance.model.api.IBaseResource

internal object ResourceLoader {
    fun asString(resource: String): String =
        try {
            object {}.javaClass.getResource(resource)!!.readText(Charsets.UTF_8)
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }

    fun asByteArray(resource: String): ByteArray =
        try {
            object {}.javaClass.getResource(resource)!!.readBytes()
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }

    inline fun <reified T : IBaseResource> asFhirResource(resource: String): T {
        val fhirContext = FhirContext.forCached(FhirVersionEnum.R4)!!
        val jsonParser = fhirContext.newJsonParser()!!
        return jsonParser.parseResource(T::class.java, asString(resource))
    }
}
