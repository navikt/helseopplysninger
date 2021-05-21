package no.nav.helse.hops.integrationtest

import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.api
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.testUtils.ResourceLoader
import no.nav.helse.hops.testUtils.TestContainerFactory
import no.nav.helse.hops.testUtils.url
import org.hl7.fhir.r4.model.Task
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URL
import kotlin.test.assertEquals

@Testcontainers
class ApplicationTest {

    @Test
    fun `hente alle tasks`() {
        populateHapiTestContainer()
        withHopsTestApplication {
            with(
                handleRequest(HttpMethod.Get, "/tasks")
            ) {
                assertEquals(HttpStatusCode.OK, response.status())

                val task = JsonConverter.parse<Task>(response.content!!)
                assertEquals(Task.TaskStatus.DRAFT, task.status)
            }
        }
    }

    private fun <R> withHopsTestApplication(testFunc: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("no.nav.security.jwt.issuers.size", "1")
                put("no.nav.security.jwt.issuers.0.issuer_name", "maskinporten")
                put("no.nav.security.jwt.issuers.0.discoveryurl", "${mockOauth2Container.url}/maskinporten/.well-known/openid-configuration")
                put("no.nav.security.jwt.issuers.0.accepted_audience", "aud-localhost")
                put("hapiserver.baseUrl", "${hapiFhirContainer.url}/fhir")
                put("hapiserver.discoveryUrl", "${mockOauth2Container.url}/default/.well-known/openid-configuration")
            }
            api()
        }) {
            testFunc()
        }
    }

    private fun populateHapiTestContainer() {
        val task = ResourceLoader.asFhirResource<Task>("/fhir/Task.json")
        val fhirClient = FhirClientFactory.create(URL("${hapiFhirContainer.url}/fhir"))
        fhirClient.update().resource(task).execute()
    }

    @Container
    val hapiFhirContainer = TestContainerFactory.hapiFhirServer()

    @Container
    val mockOauth2Container = TestContainerFactory.mockOauth2Server()
}
