package no.nav.helse.hops.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.parser.IParser
import org.hl7.fhir.instance.model.api.IBaseResource

object JsonConverter {
    // Not thread safe, new instance must therefore be created.
    fun newParser(): IParser = FhirContext
        .forCached(FhirVersionEnum.R4)
        .newJsonParser()
        .setPrettyPrint(true)
        .setStripVersionsFromReferences(false)
        .setOverrideResourceIdWithBundleEntryFullUrl(false)

    inline fun <reified R : IBaseResource> parse(json: String): R =
        newParser().parseResource(R::class.java, json)

    fun serialize(resource: IBaseResource): String =
        newParser().encodeResourceToString(resource)
}

fun IBaseResource.toJson() =
    JsonConverter.serialize(this)
