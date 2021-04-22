package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.parser.DataFormatException
import no.nav.helse.hops.testUtils.ResourceLoader
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertFailsWith

internal class KafkaFhirResourceDeserializerTest {
    @Test
    fun `deserializing valid fhir resource`() {
        val sut = KafkaFhirResourceDeserializer()

        val bytes = ResourceLoader.asByteArray("/fhir/valid-message.json")
        val result = sut.deserialize("topic", bytes) as Bundle

        assertEquals(3, result.entry.size)
    }

    @Test
    fun `deserializing invalid fhir resource`() {
        val sut = KafkaFhirResourceDeserializer()

        val bytes = "{ \"resourceType\": \"unexpected-type\" }".toByteArray()
        assertFailsWith<DataFormatException> { sut.deserialize("topic", bytes) }
    }
}
