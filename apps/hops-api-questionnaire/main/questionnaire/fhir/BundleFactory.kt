package questionnaire.fhir

import no.nav.helse.hops.fhir.JsonConverter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.InstantType
import org.hl7.fhir.r4.model.Questionnaire
import questionnaire.cache.QuestionnaireDto
import java.util.UUID

object BundleFactory {
    private val parser = JsonConverter.newParser()

    fun searchset(questionnaires: List<QuestionnaireDto>): Bundle {
        fun toBundleEntry(questionnaire: QuestionnaireDto) = Bundle.BundleEntryComponent().apply {
            fullUrl = questionnaire.url.toString()
            resource = parser.parseResource(Questionnaire::class.java, questionnaire.raw)
        }

        return Bundle().apply {
            id = UUID.randomUUID().toString()
            timestampElement = InstantType.withCurrentTime()
            type = Bundle.BundleType.SEARCHSET
            entry = questionnaires.map(::toBundleEntry)
        }
    }
}
