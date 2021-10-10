package e2e.fhir

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe

class FhirResourceTest : StringSpec({

    "can encode FhirContent to String" {
        val content = FhirResource.create()
        val string = FhirResource.encode(content)
        string shouldNotBe null
    }

    "can decode String to FhirContent" {
        val string = FhirResource.encode(FhirResource.create())!!
        val content = FhirResource.decode(string)
        content shouldNotBe null
    }

    "parsing LocalDateTime back and forth preserve its format" {
        val created = FhirResource.create()
        val decoded = FhirResource.decode(FhirResource.encode(created)!!)!!
        decoded.timestamp shouldBe created.timestamp
    }
})
