package no.nav.helse.hops.integrationTests

import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeout
import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.executeTransaction
import no.nav.helse.hops.fhir.models.Transaction
import no.nav.helse.hops.fhir.withUuidPrefixFix
import no.nav.helse.hops.main
import no.nav.helse.hops.testUtils.KafkaMock
import no.nav.helse.hops.testUtils.ResourceLoader
import no.nav.helse.hops.testUtils.TestContainerFactory
import no.nav.helse.hops.testUtils.url
import org.apache.kafka.clients.producer.Producer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URL

@Testcontainers
class ApplicationTest {
    private val producerMock = KafkaMock.createProducer()
    private val testKoinModule = module(override = true) {
        single<Producer<Unit, IBaseResource>> { producerMock }
    }

    @Test
    fun `hapi-fhir-server med bestilling skal generere response-message`() {
        populateHapiTestContainer()

        withHopsTestApplication {
            runBlocking {
                withTimeout(5000) {
                    while (producerMock.history().size == 0) delay(100)
                }
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
        val message = ResourceLoader.asFhirResource<Bundle>("/fhir/valid-message.json").withUuidPrefixFix()
        val transaction = createTransaction(message)
        val client = FhirClientFactory.create(URL("${hapiFhirContainer.url}/fhir"))
        client.executeTransaction(transaction)
    }

    @Container
    val hapiFhirContainer = TestContainerFactory.hapiFhirServer()

    @Container
    val mockOauth2Container = TestContainerFactory.mockOauth2Server()
}

private fun createTransaction(bundle: Bundle): Transaction {
    val resources = bundle.entry.map { it.resource }
    return Transaction().apply { resources.forEach { addUpsert(it, 0) } }
}
