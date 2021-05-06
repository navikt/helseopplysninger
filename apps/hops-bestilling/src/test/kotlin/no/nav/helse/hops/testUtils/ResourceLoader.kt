package no.nav.helse.hops.testUtils

import no.nav.helse.hops.fhir.JsonParser
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

    inline fun <reified R : IBaseResource> asFhirResource(resource: String) =
        JsonParser.parse<R>(asString(resource))
}
