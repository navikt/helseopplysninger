package no.nav.helse.hops.domain

import no.nav.helse.hops.fhir.messages.BaseMessage
import no.nav.helse.hops.fhir.resources
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

class GenericMessage(bundle: Bundle) : BaseMessage(bundle) {
    val data: List<Resource> get() = bundle.resources<Resource>().drop(1)
}
