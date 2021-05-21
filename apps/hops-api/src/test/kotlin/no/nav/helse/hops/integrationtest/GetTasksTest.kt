package no.nav.helse.hops.integrationtest

import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.allByUrl
import no.nav.helse.hops.testUtils.TestContainerFactory
import no.nav.helse.hops.testUtils.url
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.net.URL
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

@Testcontainers
class GetTasksTest {

    @Container
    val hapiFhirContainer = TestContainerFactory.hapiFhirServer()

    @Test
    fun `hente alle tasks`(){

        val fhirClient = FhirClientFactory.create(URL("${hapiFhirContainer.url}/fhir"))
        val tasks = fhirClient.allByUrl("Tasks").toList()
        assertNotNull(tasks)
    }
}
