package no.nav.helse.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class Hjelpemidler(
    val hjelpemiddelListe: List<HjelpemiddelListe>,
    val hjelpemiddelTotaltAntall: Int
)
