package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.domain.okWithoutWarnings
import no.nav.helse.hops.domain.toJson
import no.nav.helse.hops.testUtils.ResourceLoader
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.Test

internal class FhirResourceValidatorHapiTest {
    @Test
    fun `validate valid message bundle`() {
        val sut = FhirResourceValidatorHapi()

        val message = ResourceLoader.asFhirResource<Bundle>("/fhir/valid-message.json")
        val result = runBlocking { sut.validate(message) }

        kotlin.test.assertTrue(result.okWithoutWarnings(), result.toJson())
    }

    @Test
    fun `validate message bundle without required message-header`() {
        val sut = FhirResourceValidatorHapi()

        val message = ResourceLoader.asFhirResource<Bundle>("/fhir/invalid-message-missing-header.json")
        val result = runBlocking { sut.validate(message) }

        kotlin.test.assertFalse(result.okWithoutWarnings(), result.toJson())
    }
}
