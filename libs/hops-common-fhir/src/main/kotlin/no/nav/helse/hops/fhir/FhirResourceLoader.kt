package no.nav.helse.hops.fhir

import no.nav.helse.hops.ResourceLoader.asString
import org.hl7.fhir.instance.model.api.IBaseResource

object FhirResourceLoader {
    /** Loads a file from the class-path as a FhirResource, this includes files in the /resources dir. **/
    inline fun <reified R : IBaseResource> asResource(resource: String) =
        JsonConverter.parse<R>(asString(resource))
}
