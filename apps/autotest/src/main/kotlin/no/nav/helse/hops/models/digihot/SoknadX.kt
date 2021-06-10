package no.nav.helse.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class SoknadX(
    val bruker: Bruker,
    val brukersituasjon: Brukersituasjon,
    val date: String,
    val hjelpemidler: Hjelpemidler,
    val id: String,
    val levering: Levering
)
