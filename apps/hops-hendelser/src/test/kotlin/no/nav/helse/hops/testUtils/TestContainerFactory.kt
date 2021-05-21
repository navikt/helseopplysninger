package no.nav.helse.hops.testUtils

import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait

object TestContainerFactory {
    fun hapiFhirServer() = GenericContainer<Nothing>(
        "hapiproject/hapi:v5.3.0"
    ).apply {
        withExposedPorts(8080)
        waitingFor(Wait.forHttp("/fhir/metadata"))
    }

    fun mockOauth2Server() = GenericContainer<Nothing>(
        "docker.pkg.github.com/navikt/mock-oauth2-server/mock-oauth2-server:0.3.2"
    ).apply {
        withEnv("SERVER_PORT", "8081")
        withExposedPorts(8081)
        waitingFor(Wait.forHttp("/default/debugger"))
    }
}
