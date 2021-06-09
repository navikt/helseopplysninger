package no.nav.helse.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class TilbehorListe(
    val antall: Int,
    val hmsnr: String,
    val navn: String
)
