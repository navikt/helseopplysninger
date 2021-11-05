package questionnaire.fhir

import org.hl7.fhir.r4.model.Meta
import org.hl7.fhir.r4.model.Questionnaire
import java.util.Date

object QuestionnaireEnricher {
    fun enrich(timestamp: Date, questionnaire: Questionnaire): Questionnaire =
        questionnaire.copy().apply {
            id = url.substringAfterLast("/") + "-" + version

            if (meta == null) meta = Meta()
            meta.lastUpdated = timestamp
            meta.versionId = "1"
        }
}
