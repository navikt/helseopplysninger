package no.nav.helse.hops.hops.testdata

import no.nav.helse.hops.hops.fhir.toJson
import org.junit.jupiter.api.Test

internal class PatientKtTest {

    @Test
    fun `should create a patient`() {
        val patient = createPatient()
        println(patient.toJson())
    }
}
