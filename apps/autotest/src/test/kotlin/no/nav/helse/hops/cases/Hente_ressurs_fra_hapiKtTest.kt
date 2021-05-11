package no.nav.helse.hops.cases

import no.nav.helse.hops.fhir.toJson
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class Hente_ressurs_fra_hapiKtTest {

    @Test
    @Disabled
    fun henteRessursFraFhirTest() {
        val bundle = henteRessursFraFhir()
        println(bundle.toJson())
    }
}
