package no.nav.helse.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class DigihotKvittering(
    val eventId: String,
    val eventName: String,
    val fodselNrBruker: String,
    val fodselNrInnsender: String,
    val kommunenavn: String,
    val signatur: String,
    val soknad: Soknad
)
