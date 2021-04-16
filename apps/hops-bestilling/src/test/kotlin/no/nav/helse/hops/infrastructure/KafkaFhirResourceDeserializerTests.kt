package no.nav.helse.hops.infrastructure

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class KafkaFhirResourceDeserializerTests {
    @Test
    fun `Dividing by zero should throw the DivideByZeroException`() {
        assertEquals(2, 1)
    }
}