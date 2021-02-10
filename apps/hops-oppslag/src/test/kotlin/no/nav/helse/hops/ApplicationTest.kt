package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.hops.fkr.FkrKoinModule
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.junit.Test
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.BeforeAll
import org.koin.dsl.module
import org.koin.ktor.ext.modules
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `Search for practitioners by HPR-NR`() {
        withHopsTestApplication {
            application.modules(testKoinModule)
            with(handleRequest(HttpMethod.Get, "/behandler/1234")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Våge", response.content)
            }
        }
    }

    @Test
    fun hello_withMissingJWTShouldGive_401_Unauthorized() {
        withHopsTestApplication {
            with(handleRequest(HttpMethod.Get, "/protected")) {
                assertEquals(HttpStatusCode.Unauthorized, response.status())
            }
        }
    }

    private fun <R> withHopsTestApplication(test: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            doConfig()
            module()
        }) {
            test()
        }
    }

    private fun Application.doConfig(
        acceptedIssuer: String = "default",
        acceptedAudience: String = "default"
    ) {
        (environment.config as MapApplicationConfig).apply {
            put("${FkrKoinModule.CONFIG_NAMESPACE}.host", FkrClientMock.HOST)
            put("${FkrKoinModule.CONFIG_NAMESPACE}.tokenUrl", "http://token-test.no")
            put("${FkrKoinModule.CONFIG_NAMESPACE}.clientId", "test-client-id")
            put("${FkrKoinModule.CONFIG_NAMESPACE}.clientSecret", "test-secret")
            put("${FkrKoinModule.CONFIG_NAMESPACE}.scope", "test-scope")
            put("no.nav.security.jwt.issuers.size", "1")
            put("no.nav.security.jwt.issuers.0.issuer_name", acceptedIssuer)
            put("no.nav.security.jwt.issuers.0.discoveryurl", "${server.wellKnownUrl(acceptedIssuer)}")
            put("no.nav.security.jwt.issuers.0.accepted_audience", acceptedAudience)
            put("no.nav.security.jwt.issuers.0.cookie_name", "selvbetjening-idtoken")
        }
    }

    private val testKoinModule = module(override = true) {
        single(FkrKoinModule.CLIENT) { FkrClientMock.client }
    }

    companion object {
        val server = MockOAuth2Server()

        @BeforeAll
        @JvmStatic
        fun before() {
            server.start()

        }

        @AfterAll
        @JvmStatic
        fun after() {
            server.shutdown()
        }
    }
}