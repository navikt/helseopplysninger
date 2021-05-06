package no.nav.helse.hops.integrationTests

import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.module
import no.nav.helse.hops.testUtils.url
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ApplicationTest {
    @Test
    fun `my test example`() {
        withHopsTestApplication {
            Thread.sleep(1000)
        }
    }

    private fun <R> withHopsTestApplication(testFunc: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("fhirServer.baseUrl", "${hapiFhirContainer.url}/fhir")
                put("fhirServer.discoveryUrl", "${mockOauth2Container.url}/default/.well-known/openid-configuration")
            }
            module()
        }) {
            testFunc()
        }
    }

    @Container
    val hapiFhirContainer = GenericContainer<Nothing>(
        "hapiproject/hapi"
    )
        .apply {
            withExposedPorts(8080)
            waitingFor(Wait.forHttp("/fhir/Task"))
        }

    @Container
    val mockOauth2Container = GenericContainer<Nothing>(
        "docker.pkg.github.com/navikt/mock-oauth2-server/mock-oauth2-server:0.3.2"
    )
        .apply {
            withEnv("SERVER_PORT", "8081")
            withExposedPorts(8081)
            waitingFor(Wait.forHttp("/default/debugger"))
        }
}
