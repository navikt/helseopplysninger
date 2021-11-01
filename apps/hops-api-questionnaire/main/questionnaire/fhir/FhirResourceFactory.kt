package questionnaire.fhir

import no.nav.helse.hops.fhir.JsonConverter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.Questionnaire
import java.util.UUID

object FhirResourceFactory {
    fun searchset(questionnaires: Collection<Questionnaire>): Bundle {
        fun toBundleEntry(questionnaire: Questionnaire) = Bundle.BundleEntryComponent().apply {
            resource = questionnaire
        }

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.SEARCHSET
            entry = questionnaires.map(::toBundleEntry)
        }
    }

    fun questionnaire(raw: String): Questionnaire = JsonConverter.parse(raw)
}
