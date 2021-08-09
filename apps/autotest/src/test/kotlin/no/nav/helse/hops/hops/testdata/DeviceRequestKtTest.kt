package no.nav.helse.hops.hops.testdata

import no.nav.helse.hops.hops.fhir.toJson
import no.nav.helse.hops.hops.models.HjelpemiddelRequest
import org.junit.jupiter.api.Test
import java.time.LocalDate
import kotlin.test.assertTrue

internal class DeviceRequestKtTest {

    @Test
    fun createDeviceRequestTest() {
        val deviceRequest = createDeviceRequest(
            HjelpemiddelRequest(
                beskrivelse = "beskrivelse",
                brukerFnr = "12345678910",
                brukerNavn = "Kalle Kamel",
                innsenderFnr = "12345678910",
                innsenderNavn = "Frode Fysioterapaut",
                produktEkstra = "Ekstra info om produktet",
                produktHmsNr = "123",
                produktNavn = "Gåstol for gamle",
                soknadDate = LocalDate.parse("2020-01-01"),
                soknadId = "123",
            )
        )
        val deviceRequestJson = deviceRequest.toJson()
        println(deviceRequestJson)
        assertTrue(deviceRequestJson.contains("DeviceRequest"))
        assertTrue(deviceRequestJson.startsWith("{"))
        assertTrue(deviceRequestJson.endsWith("}"))
    }
}
