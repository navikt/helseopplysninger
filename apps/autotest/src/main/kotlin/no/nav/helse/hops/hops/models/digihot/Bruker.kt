package no.nav.helse.hops.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class Bruker(
    val etternavn: String,
    val fnummer: String,
    val fornavn: String,
    val signatur: String,
    val telefonNummer: String
)
