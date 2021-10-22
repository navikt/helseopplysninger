package no.nav.helse.hops.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.instance.model.api.IBaseResource
import java.io.ByteArrayOutputStream
import java.io.OutputStreamWriter

object JsonConverter {
    // Not thread safe, new instance must therefore be created.
    fun newParser(pretty: Boolean = false): IParser = FhirContext
        .forCached(FhirVersionEnum.R4)
        .newJsonParser()
        .setPrettyPrint(pretty)
        .setStripVersionsFromReferences(false)
        .setOverrideResourceIdWithBundleEntryFullUrl(false)

    inline fun <reified R : IBaseResource> parse(json: String): R =
        newParser().parseResource(R::class.java, json)

    inline fun <reified R : IBaseResource> parse(json: ByteArray): R =
        json.inputStream().use {
            newParser().parseResource(R::class.java, it)
        }

    fun serialize(resource: IBaseResource, pretty: Boolean): String =
        newParser(pretty).encodeResourceToString(resource)
}

fun IBaseResource.toJson(pretty: Boolean = true) =
    JsonConverter.serialize(this, pretty)

fun IBaseResource.toJsonByteArray(pretty: Boolean = false): ByteArray =
    ByteArrayOutputStream().use { stream ->
        OutputStreamWriter(stream).use { writer ->
            val parser = JsonConverter.newParser(pretty)
            parser.encodeResourceToWriter(this, writer)
        }

        return stream.toByteArray()
    }
