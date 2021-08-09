package no.nav.helse.hops.hops

import kotlinx.coroutines.runBlocking
import no.nav.helse.hops.hops.auth.Auth
import no.nav.helse.hops.hops.utils.Fixtures
import org.hl7.fhir.r4.model.ResourceType
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import java.util.Date
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class AuthClientTest {

    @Test
    @Disabled
    fun `should give a token`() {
        val auth = Auth()
        val scope = "myscope"
        val token = runBlocking {
            auth.token(scope)
        }
        val something = true
        assertEquals(something, true)

        if (token != null) {
            assertTrue { token.expirationTime > Date() }
        }
    }

    @Test
    fun `read file`() {
        val resource = Fixtures().bestillingsBundle()
        assertEquals(resource.resourceType, ResourceType.Bundle)
    }
}
