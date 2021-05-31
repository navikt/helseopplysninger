package no.nav.helse.hops.utils

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

internal class HashStringTest {

    @Test
    fun md5Test() {
        val hashedString = HashString.md5("dfafdsa")
        assertEquals(hashedString.uppercase(), "498FD1D9D7CE83CA1CBDDB789D2F9786")
    }
}
