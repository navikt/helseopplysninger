package no.nav.helse.hops.testUtils

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import java.time.Duration

object TestContainerFactory {
    fun hapiFhirServer() = GenericContainer<Nothing>(
        "hapiproject/hapi:v5.3.0"
    ).apply {
        withExposedPorts(8080)
        waitingFor(Wait.forHttp("/fhir/metadata").withStartupTimeout(Duration.ofMinutes(2)))
    }

    fun mockOauth2Server() = GenericContainer<Nothing>(
        "ghcr.io/navikt/mock-oauth2-server:0.3.3"
    ).apply {
        withEnv("SERVER_PORT", "8081")
        withExposedPorts(8081)
        waitingFor(Wait.forHttp("/default/debugger"))
    }
}
