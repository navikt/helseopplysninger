package no.nav.helse.hops.fhir

import org.hl7.fhir.r4.model.IdType
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import kotlin.test.assertEquals

class IdTypeExtTest {
    @Test
    fun `toUriType() with valid UUID should return UriType`() {
        val id = IdType("edaeae77-92c9-4ed1-b2d1-e15b953e8af8")
        val result = id.toUriType()
        assertEquals("urn:uuid:edaeae77-92c9-4ed1-b2d1-e15b953e8af8", result.valueAsString)
    }

    @Test
    fun `toUriType() with invalid UUID should throw IllegalArgumentException`() {
        val id = IdType("not-an-uuid")
        assertThrows<IllegalArgumentException> { id.toUriType() }
    }
}
