package no.nav.helse.hops.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.instance.model.api.IBaseResource

object JsonConverter {
    fun newParser(pretty: Boolean): IParser = FhirContext
        .forCached(FhirVersionEnum.R4)
        .newJsonParser()
        .setPrettyPrint(pretty)
        .setOverrideResourceIdWithBundleEntryFullUrl(false)

    inline fun <reified R : IBaseResource> parse(json: String): R =
        newParser(false).parseResource(R::class.java, json)

    fun serialize(resource: IBaseResource, pretty: Boolean): String =
        newParser(pretty).encodeResourceToString(resource)
}

fun IBaseResource.toJson(pretty: Boolean = true) =
    JsonConverter.serialize(this, pretty)
