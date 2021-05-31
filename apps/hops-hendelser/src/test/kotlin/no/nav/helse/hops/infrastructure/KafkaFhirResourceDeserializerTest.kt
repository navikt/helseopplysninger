package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.ResourceLoader
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

internal class KafkaFhirResourceDeserializerTest {
    @Test
    fun `deserializing valid fhir json should return Resource`() {
        val sut = KafkaFhirResourceDeserializer()

        val bytes = ResourceLoader.asByteArray("/fhir/valid-message.json")
        val result = sut.deserialize("topic", bytes) as Bundle

        assertEquals(3, result.entry.size)
    }

    @Test
    fun `deserializing invalid fhir json should return NULL`() {
        val sut = KafkaFhirResourceDeserializer()

        val bytes = "{ \"resourceType\": \"unexpected-type\" }".toByteArray()
        val result = sut.deserialize("topic", bytes)

        assertNull(result)
    }
}
