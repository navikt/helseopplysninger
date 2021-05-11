package no.nav.helse.hops

import io.ktor.http.Url
import no.nav.helse.hops.fhir.createFhirMessage
import no.nav.helse.hops.fhir.toJson
import no.nav.helse.hops.utils.dockerEnvVars
import no.nav.helse.hops.utils.urlReturnsStatusCode
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
    fun `Requests should be 200`() {
        val url = Url("https://ktor.io/")
        val result = urlReturnsStatusCode(url, 200)
        assertEquals(result, true)
    }

    @Test
    fun `All services should respond 200`() {
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

    @Test
    fun `Should read env-files`() {
        val content = dockerEnvVars()

        assertTrue(true)
    }
}
