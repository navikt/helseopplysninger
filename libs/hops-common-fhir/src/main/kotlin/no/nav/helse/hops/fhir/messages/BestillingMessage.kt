package no.nav.helse.hops.fhir.messages

import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Questionnaire
import org.hl7.fhir.r4.model.Task

class BestillingMessage(bundle: Bundle) : BaseMessage(bundle) {
    init {
        requireEntryCount(3)
        requireEntry<Task>(1)
        requireEntry<Questionnaire>(2)
    }

    val task: Task get() = resource(1)
    val questionnaire: Questionnaire get() = resource(2)
}
