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
    fun `Search for practitioners by HPR-NR`() {
        withTestApplication(Application::module) {
            application.modules(testKoinModule)
            withTestConfig()
            with(handleRequest(HttpMethod.Get, "/behandler/1234")) {
                assertEquals(HttpStatusCode.OK, response.status())
                assertEquals("Våge", response.content)
            }
        }
    }
}

private fun TestApplicationEngine.withTestConfig(): MapApplicationConfig {
    return (environment.config as MapApplicationConfig).apply {
        put("${FkrKoinModule.CONFIG_NAMESPACE}.host", FkrClientMock.HOST)
        put("${FkrKoinModule.CONFIG_NAMESPACE}.tokenUrl", "http://token-test.no")
        put("${FkrKoinModule.CONFIG_NAMESPACE}.clientId", "test-client-id")
        put("${FkrKoinModule.CONFIG_NAMESPACE}.clientSecret", "test-secret")
        put("${FkrKoinModule.CONFIG_NAMESPACE}.scope", "test-scope")
    }
}

private val testKoinModule = module(override = true) {
    single(FkrKoinModule.CLIENT) { FkrClientMock.client }
}