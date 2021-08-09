package no.nav.helse.hops.testdata

import org.hl7.fhir.r4.model.Communication
import org.hl7.fhir.r4.model.Reference
import java.util.UUID

/**
 * https://www.hl7.org/fhir/communication.html
 */
fun createIncommingCommunication(
    sentBy: Reference,
    aboutWho: Reference,
    aboutWhat: List<Reference>
) = Communication().apply {
    id = UUID.randomUUID().toString()
    status = Communication.CommunicationStatus.COMPLETED
    subject = aboutWho
    sender = sentBy
    recipient = listOf(lagNavRef())
    about = aboutWhat
}

fun createOutgoingCommunication(
    receiver: Reference,
    aboutWho: Reference,
    aboutWhat: List<Reference>
) = Communication().apply {
    id = UUID.randomUUID().toString()
    status = Communication.CommunicationStatus.COMPLETED
    subject = aboutWho
    sender = lagNavRef()
    recipient = listOf(receiver)
    about = aboutWhat
}

fun lagNavRef() = lagOrgRef("889640782", "Arbeids- og velferdsetaten")
