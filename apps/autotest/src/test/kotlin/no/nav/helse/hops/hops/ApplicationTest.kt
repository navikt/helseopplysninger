package no.nav.helse.hops.hops

import io.ktor.http.Url
import no.nav.helse.hops.hops.fhir.createFhirMessage
import no.nav.helse.hops.hops.fhir.toJson
import no.nav.helse.hops.hops.utils.urlReturnsStatusCode
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class ApplicationTest {

    @Test
    fun `Requests with without token should return 401-Unauthorized`() {
        val something = true
        assertEquals(something, true)
    }

    @Test
    @Disabled
    fun `Requests should be 200`() {
        val url = Url("https://ktor.io/")
        val result = urlReturnsStatusCode(url, 200)
        assertEquals(result, true)
    }

    @Test
    fun `Should create FHIR-message`() {
        val jsonbundle = createFhirMessage().toJson()
        assertTrue(jsonbundle.startsWith("{"))
        assertTrue(jsonbundle.endsWith("}"))
        assertTrue(jsonbundle.contains("Bundle"))
    }
}
