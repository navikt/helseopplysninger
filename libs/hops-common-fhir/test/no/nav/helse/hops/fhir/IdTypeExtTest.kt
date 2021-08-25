package no.nav.helse.hops.fhir

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import org.hl7.fhir.r4.model.IdType

class IdTypeExtTest : StringSpec({
    "toUriType() with valid UUID should return UriType" {
        val id = IdType("edaeae77-92c9-4ed1-b2d1-e15b953e8af8")
        val result = id.toUriType()
        result.valueAsString shouldBe "urn:uuid:edaeae77-92c9-4ed1-b2d1-e15b953e8af8"
    }

    "toUriType() with invalid UUID should throw IllegalArgumentException" {
        val id = IdType("not-an-uuid")
        shouldThrow<IllegalArgumentException> { id.toUriType() }
    }
})
