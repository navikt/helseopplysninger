package no.nav.helse.hops.integrationtest

import no.nav.helse.hops.fhir.FhirClientFactory
import no.nav.helse.hops.fhir.allByUrl
import no.nav.helse.hops.testUtils.url
import org.junit.jupiter.api.Test
import java.net.URL
import kotlin.test.assertFalse
import kotlin.test.assertNotNull

class GetTasksTest {
    @Test
    fun `hente alle tasks`(){
        val fhirClient = FhirClientFactory.create(URL("/fhir"))
        val tasks = fhirClient.allByUrl("Tasks").toList()
        assertNotNull(tasks)
    }
}
