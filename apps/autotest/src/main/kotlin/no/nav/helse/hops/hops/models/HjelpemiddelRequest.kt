package no.nav.helse.hops.hops.models

import java.time.LocalDate

data class HjelpemiddelRequest(
    val beskrivelse: String?,
    val brukerFnr: String,
    val brukerNavn: String,
    val innsenderFnr: String,
    val innsenderNavn: String?,
    val produktEkstra: String?,
    val produktHmsNr: String,
    val produktNavn: String?,
    val soknadDate: LocalDate,
    val soknadId: String,
)
