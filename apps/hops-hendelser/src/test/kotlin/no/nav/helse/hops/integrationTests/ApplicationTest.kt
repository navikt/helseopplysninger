package no.nav.helse.hops.integrationTests

import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.toUriType
import no.nav.helse.hops.fhir.withUuidPrefixFix
import no.nav.helse.hops.module
import no.nav.helse.hops.testUtils.ResourceLoader
import no.nav.helse.hops.testUtils.url
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.junit.jupiter.api.Test
import org.testcontainers.containers.GenericContainer
import org.testcontainers.containers.wait.strategy.Wait
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URL

@Testcontainers
class ApplicationTest {
    @Test
    fun `my test example`() {
        withFhirData("/fhir/valid-message.json")

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

    private fun withFhirData(fhirMessage: String) {
        val message = ResourceLoader.asFhirResource<Bundle>(fhirMessage).withUuidPrefixFix()
        val transaction = message.entry.map { it.resource }.asTransaction()
        val client = FhirClientFactory.create(URL("${hapiFhirContainer.url}/fhir"))
        client.transaction().withBundle(transaction).execute()
    }

    @Container
    val hapiFhirContainer = GenericContainer<Nothing>(
        "hapiproject/hapi"
    ).apply {
        withExposedPorts(8080)
        waitingFor(Wait.forHttp("/fhir/Task"))
    }

    @Container
    val mockOauth2Container = GenericContainer<Nothing>(
        "docker.pkg.github.com/navikt/mock-oauth2-server/mock-oauth2-server:0.3.2"
    ).apply {
        withEnv("SERVER_PORT", "8081")
        withExposedPorts(8081)
        waitingFor(Wait.forHttp("/default/debugger"))
    }
}

private fun List<Resource>.asTransaction() =
    Bundle().apply {
        type = Bundle.BundleType.TRANSACTION
        entry = map {
            Bundle.BundleEntryComponent().apply {
                resource = it
                fullUrlElement = it.idElement.toUriType()
                request = Bundle.BundleEntryRequestComponent().apply {
                    method = Bundle.HTTPVerb.PUT
                    url = "${it.fhirType()}/${it.id}"
                    ifMatch = "W/\"0\"" // only upsert if resource does not already exist with same ID.
                }
            }
        }
    }
