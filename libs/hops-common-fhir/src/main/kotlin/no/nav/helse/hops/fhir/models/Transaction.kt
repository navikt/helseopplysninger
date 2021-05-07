package no.nav.helse.hops.fhir.models

import no.nav.helse.hops.fhir.toUriType
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource

class Transaction {
    val bundle = Bundle().apply {
        type = Bundle.BundleType.TRANSACTION
    }

    fun addUpsert(res: Resource, ifMatchVid: Int) {
        val entry = Bundle.BundleEntryComponent().apply {
            resource = res
            fullUrlElement = res.idElement.toUriType()
            request = Bundle.BundleEntryRequestComponent().apply {
                method = Bundle.HTTPVerb.PUT
                url = "${res.fhirType()}/${res.id}"
                ifMatch = "W/\"$ifMatchVid\""
            }
        }

        bundle.addEntry(entry)
    }
}
