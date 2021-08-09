package no.nav.helse.hops.hops.utils.fnr

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class FoedselsnummerGeneratorTest {

    @Test
    fun foedselsnummer() {
        val fnr = FoedselsnummerGenerator().foedselsnummer()
        assertEquals(11, fnr.asString.length)
    }
}
