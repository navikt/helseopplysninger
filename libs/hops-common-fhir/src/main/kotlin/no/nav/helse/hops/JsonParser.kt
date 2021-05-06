package no.nav.helse.hops

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import org.hl7.fhir.instance.model.api.IBaseResource

object JsonParser {
    inline fun <reified R : IBaseResource> parse(json: String): R {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)
        return ctx.newJsonParser().parseResource(R::class.java, json)
    }
}

fun IBaseResource.toJson(): String {
    val ctx = FhirContext.forCached(FhirVersionEnum.R4)
    return ctx.newJsonParser().encodeResourceToString(this)
}
