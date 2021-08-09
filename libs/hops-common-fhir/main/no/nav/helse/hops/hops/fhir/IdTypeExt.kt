package no.nav.helse.hops.hops.fhir

import no.nav.helse.hops.hops.toUri
import org.hl7.fhir.r4.model.IdType
import org.hl7.fhir.r4.model.UriType
import java.util.UUID

/** Creates a URI type with urn:uuid prefix.
 * @exception IllegalArgumentException if ID is not a valid UUID. **/
fun IdType.toUriType() = UriType(UUID.fromString(idPart).toUri())
