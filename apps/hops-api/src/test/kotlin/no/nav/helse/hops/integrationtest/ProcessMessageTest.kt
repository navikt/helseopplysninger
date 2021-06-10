package no.nav.helse.hops.integrationtest

import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.setBody
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.api
import no.nav.helse.hops.fhir.FhirResourceLoader
import no.nav.helse.hops.fhir.JsonConverter
import no.nav.helse.hops.fhir.resources
import no.nav.helse.hops.fhir.toJson
import no.nav.helse.hops.testUtils.TestContainerFactory
import no.nav.helse.hops.testUtils.url
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.Resource
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import kotlin.test.assertEquals

@Testcontainers
class ProcessMessageTest {

    @Test
    fun `process and retrieve message`() {
        withHopsTestApplication {
            with(
                handleRequest(HttpMethod.Post, "/\$process-message") {
                    val message = FhirResourceLoader.asResource<Bundle>("/fhir/message-to-process.json")
                    setBody(message.toJson())
                    addHeader("Content-Type", "application/fhir+json")
                }
            ) {
                assertEquals(HttpStatusCode.OK, response.status())
            }

            with(
                handleRequest(
                    HttpMethod.Get,
                    "/Bundle?message.destination-uri=http%3A%2F%2Fnav.no%2Fhops&_lastUpdated=gt2015-03-01T02%3A00%3A02%2B01%3A00"
                )
            ) {
                assertEquals(HttpStatusCode.OK, response.status())

                val searchResult = JsonConverter.parse<Bundle>(response.content!!)
                val message = searchResult.resources<Resource>().single() as Bundle
                assertEquals(3, message.entry.count())
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

    @Container
    val hapiFhirContainer = TestContainerFactory.hapiFhirServer()

    @Container
    val mockOauth2Container = TestContainerFactory.mockOauth2Server()
}
