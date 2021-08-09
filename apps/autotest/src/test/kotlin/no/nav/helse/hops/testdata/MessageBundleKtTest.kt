package no.nav.helse.hops.testdata

import no.nav.helse.hops.fhir.toJson
import org.hl7.fhir.r4.model.Communication
import org.junit.jupiter.api.Test
import kotlin.test.assertTrue

internal class MessageBundleKtTest {

    @Test
    fun createMassageBundleTest() {
        val bundle = createCommunicationBundle("fdgasd", listOf(), Communication())
        val bundleJson = bundle.toJson()
        println(bundleJson)
        assertTrue(bundleJson.contains("Bundle"))
        assertTrue(bundleJson.startsWith("{"))
        assertTrue(bundleJson.endsWith("}"))
    }
}
