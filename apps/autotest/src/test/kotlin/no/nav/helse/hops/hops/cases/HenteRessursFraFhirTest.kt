package no.nav.helse.hops.hops.cases

import no.nav.helse.hops.hops.fhir.toJson
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test

internal class HenteRessursFraFhirTest {

    @Test
    @Disabled
    fun henteRessursFraFhirTest() {
        val bundle = henteRessursFraFhir()
        println(bundle.toJson())
    }
}
