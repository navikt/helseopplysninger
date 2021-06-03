package no.nav.helse.hops.integrationTests

import ca.uhn.fhir.rest.server.exceptions.ResourceNotFoundException
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.FhirResourceLoader
import no.nav.helse.hops.fhir.client.FhirClientHapi
import no.nav.helse.hops.fhir.messages.OkResponseMessage
import no.nav.helse.hops.fhir.resources
import no.nav.helse.hops.fhir.withUuidPrefixFix
import no.nav.helse.hops.main
import no.nav.helse.hops.testUtils.KafkaMock
import no.nav.helse.hops.testUtils.TestContainerFactory
import no.nav.helse.hops.testUtils.url
import org.apache.kafka.clients.producer.Producer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.MessageHeader
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URL
import kotlin.test.assertEquals

@Testcontainers
class ApplicationTest {
    private val producerMock = KafkaMock.createProducer()
    private val testKoinModule = module(override = true) {
        single<Producer<Unit, IBaseResource>> { producerMock }
    }

    @Test
    fun `hapi-fhir-server med bestilling skal generere response-message paa kafka og lagre kopi i hapi`() {
        populateHapiTestContainer()
        withHopsTestApplication {
            runBlocking {
                val kafkaMsgHeader = withTimeout(10000) {
                    while (producerMock.history().size == 0) delay(100)
                    val kafkaMsg = OkResponseMessage(producerMock.history().single().value() as Bundle)
                    kafkaMsg.header
                }

                val hapiMsgHeader = withTimeout(10000) {
                    val fhirClient = FhirClientFactory.create(URL("${hapiFhirContainer.url}/fhir"))
                    var messageHeader: MessageHeader? = null
                    do {
                        try {
                            messageHeader = fhirClient
                                .read()
                                .resource(MessageHeader::class.java)
                                .withId(kafkaMsgHeader.idElement.idPart)
                                .execute()
                        } catch (ex: ResourceNotFoundException) {
                            delay(100)
                        }
                    } while (messageHeader == null)
                    return@withTimeout messageHeader
                }

                assertEquals(kafkaMsgHeader.idElement.idPart, hapiMsgHeader.idElement.idPart)
            }
        }
    }

    private fun <R> withHopsTestApplication(testFunc: TestApplicationEngine.() -> R): R {
        return withTestApplication({
            (environment.config as MapApplicationConfig).apply {
                put("fhirServer.baseUrl", "${hapiFhirContainer.url}/fhir")
                put("fhirServer.discoveryUrl", "${mockOauth2Container.url}/default/.well-known/openid-configuration")
            }
            main(testKoinModule)
        }) {
            testFunc()
        }
    }

    private fun populateHapiTestContainer() {
        val message = FhirResourceLoader.asResource<Bundle>("/fhir/valid-message.json").withUuidPrefixFix()
        val fhirClient = FhirClientHapi(FhirClientFactory.create(URL("${hapiFhirContainer.url}/fhir")))

        runBlocking {
            fhirClient.upsertAsTransaction(message.resources())
        }
    }

    @Container
    val hapiFhirContainer = TestContainerFactory.hapiFhirServer()

    @Container
    val mockOauth2Container = TestContainerFactory.mockOauth2Server()
}
