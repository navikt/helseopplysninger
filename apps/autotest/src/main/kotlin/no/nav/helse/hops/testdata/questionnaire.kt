package no.nav.helse.hops.testdata

import no.nav.helse.hops.utils.HashString
import org.hl7.fhir.r4.model.Questionnaire
import java.util.UUID

fun createItem(text: String): Questionnaire.QuestionnaireItemComponent {
    val item = Questionnaire.QuestionnaireItemComponent()
    item.linkId = HashString.md5(text)
    item.text = text
    item.type = Questionnaire.QuestionnaireItemType.TEXT
    return item
}

fun createQuestionnaire(): Questionnaire {
    val questionnaire = Questionnaire()
    questionnaire.copyright = "NAV"
    questionnaire.id = UUID.randomUUID().toString()
    questionnaire.item = mutableListOf()
    questionnaire.item.add(createItem("Ett spørsmål"))
    questionnaire.item.add(createItem("Ett annet spørsmål"))
    questionnaire.item.add(createItem("Siste spørsmål"))
    return questionnaire
}
