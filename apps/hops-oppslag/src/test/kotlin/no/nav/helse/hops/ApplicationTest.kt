package no.nav.helse.hops

import io.ktor.application.*
import io.ktor.client.*
import io.ktor.client.engine.mock.*
import io.ktor.config.*
import io.ktor.http.*
import io.ktor.server.testing.*
import org.junit.Test
import org.koin.dsl.module
import org.koin.ktor.ext.modules
import kotlin.test.assertEquals

class ApplicationTest {
    @Test
    fun `1 + 2 = 3`() {
        assertEquals(3, 3, "1 + 2 should equal 3")
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
                assertEquals("dette er en test", response.content)
            }
        }
    }
}

private fun TestApplicationEngine.withTestConfig(): MapApplicationConfig {
    return (environment.config as MapApplicationConfig).apply {
        put("${CONFIG_NAMESPACE}.hostUrl", "http://hello.no")
        put("${CONFIG_NAMESPACE}.tokenUrl", "http://hello.no")
        put("${CONFIG_NAMESPACE}.clientId", "test-client-id")
        put("${CONFIG_NAMESPACE}.clientSecret", "test-secret")
        put("${CONFIG_NAMESPACE}.scope", "test-scope")
    }
}

private val testModule = module(override = true) {
    single(FKR_CLIENT) { testFkrClient }
}

private val testFkrClient = HttpClient(MockEngine) {
    engine {
        addHandler { respond("dette er en test") }
    }
}