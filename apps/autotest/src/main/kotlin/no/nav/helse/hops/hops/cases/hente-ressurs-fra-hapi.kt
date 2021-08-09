package no.nav.helse.hops.hops.cases

import no.nav.helse.hops.hops.utils.createFhirClient
import org.hl7.fhir.r4.model.Bundle

fun henteRessursFraFhir(): Bundle {
    val fhirClient = createFhirClient()

    return fhirClient
        .search<Bundle>()
        .byUrl("Task")
        .execute()
}
