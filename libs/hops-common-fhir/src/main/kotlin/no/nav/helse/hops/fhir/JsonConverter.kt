package no.nav.helse.hops.fhir

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.hl7.fhir.instance.model.api.IBaseResource

object JsonConverter {
    inline fun <reified R : IBaseResource> parse(json: String): R {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)
        return ctx.newJsonParser().parseResource(R::class.java, json)
    }

    fun serialize(resource: IBaseResource): String {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)
        return ctx.newJsonParser().encodeResourceToString(resource)
    }
}

fun IBaseResource.toJson() = JsonConverter.serialize(this)
