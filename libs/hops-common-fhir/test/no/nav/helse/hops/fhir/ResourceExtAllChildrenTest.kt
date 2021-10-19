package no.nav.helse.hops.fhir

import kotlin.test.assertEquals
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference
import org.junit.jupiter.api.Test

class ResourceExtAllChildrenTest {
    @Test
    fun `allChildren should return all child elements of type`() {
        val resource = Patient().apply {
            generalPractitioner = listOf(
                Reference("Practitioner/hello1"),
                Reference("Practitioner/hello2")
            )
            contact = listOf(
                Patient.ContactComponent().apply {
                    organization = Reference("Organization/hello3")
                }
            )
        }

        val refs = resource.allChildren<Reference>().toList()
        assertEquals(3, refs.count())
    }
}
