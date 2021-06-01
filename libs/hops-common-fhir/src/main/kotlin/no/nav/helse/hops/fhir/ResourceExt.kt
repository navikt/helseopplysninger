package no.nav.helse.hops.fhir

import org.hl7.fhir.r4.model.Resource

/** See https://www.hl7.org/fhir/http.html#versioning **/
fun Resource.weakEtag() = "W/\"${meta?.versionId ?: "0"}\""
