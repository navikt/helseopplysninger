package no.nav.helse.hops.integrationTests

import org.hl7.fhir.r4.model.InstantType
import org.junit.jupiter.api.Test
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Date

class TimeFormatTest {
    @Test
    fun timeFormat() {
        val instant = InstantType.withCurrentTime()
        println(instant.valueAsString)
        val s = LocalDateTime.now().atZone(ZoneId.systemDefault()).truncatedTo(ChronoUnit.MILLIS).format(DateTimeFormatter.ISO_INSTANT)
        println(s)
    }
}

private fun Date.toLocalDateTime(): LocalDateTime = LocalDateTime.ofInstant(toInstant(), ZoneId.systemDefault())
private fun Date.toFhirString(): String = InstantType(this).valueAsString
