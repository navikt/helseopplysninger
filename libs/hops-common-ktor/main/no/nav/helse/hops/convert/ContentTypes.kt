package no.nav.helse.hops.convert

import io.ktor.http.ContentType

object ContentTypes {
    val fhirJson = ContentType("application", "fhir+json")
    val fhirJsonR4 = fhirJson.withParameter("fhirVersion", "4.0")
}
