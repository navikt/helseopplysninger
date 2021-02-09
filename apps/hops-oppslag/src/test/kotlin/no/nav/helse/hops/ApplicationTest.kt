package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import no.nav.helse.hops.fkr.FkrKoinModule
import org.junit.Test
import org.koin.dsl.module
import org.koin.ktor.ext.modules
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `1 + 2 = 3`() {
        assertEquals(3, 3, "1 + 2 should equal 3")
        assertEquals("application/json", ContentType.Application.Json.toString())
    }

    @Test
    fun testHelloEndpoint() {
        withTestApplication(Application::module) {
            withTestConfig()
            with(handleRequest(HttpMethod.Get, "/Hello")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Hello!", response.content)
            }
        }
    }

    @Test
    fun testPractitionerEndpoint() {
        withTestApplication(Application::module) {
            application.modules(testModule)
            withTestConfig()
            with(handleRequest(HttpMethod.Get, "/Practitioner")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Våge", response.content)
            }
        }
    }
}

private fun TestApplicationEngine.withTestConfig(): MapApplicationConfig {
    return (environment.config as MapApplicationConfig).apply {
        put("${FkrKoinModule.CONFIG_NAMESPACE}.host", FkrMock.HOST)
        put("${FkrKoinModule.CONFIG_NAMESPACE}.tokenUrl", "http://token-test.no")
        put("${FkrKoinModule.CONFIG_NAMESPACE}.clientId", "test-client-id")
        put("${FkrKoinModule.CONFIG_NAMESPACE}.clientSecret", "test-secret")
        put("${FkrKoinModule.CONFIG_NAMESPACE}.scope", "test-scope")
    }
}

private val testModule = module(override = true) {
    single(FkrKoinModule.CLIENT) { FkrMock.client }
}