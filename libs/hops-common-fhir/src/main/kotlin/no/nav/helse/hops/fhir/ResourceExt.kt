package no.nav.helse.hops.fhir

import org.hl7.fhir.r4.model.Resource
import java.util.UUID

/** See https://www.hl7.org/fhir/http.html#versioning **/
fun Resource.weakEtag() = "W/\"${meta?.versionId ?: "0"}\""

/** IdPart as UUID. **/
fun Resource.idAsUUID() = UUID.fromString(idElement.idPart)!!
