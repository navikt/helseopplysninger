package no.nav.helse.hops.fhir

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.hl7.fhir.r4.model.Patient
import org.hl7.fhir.r4.model.Reference

class ResourceExtAllChildrenTest : StringSpec({
    "allChildren should return all child elements of type" {
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
        refs.count() shouldBe 3
    }
})
