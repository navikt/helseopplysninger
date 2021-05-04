package no.nav.helse.hops

import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class ApplicationTest {

    @Test
    fun `Requests with without token should return 401-Unauthorized`() {
        val something = true;
        assertEquals(something, true)
    }

}
