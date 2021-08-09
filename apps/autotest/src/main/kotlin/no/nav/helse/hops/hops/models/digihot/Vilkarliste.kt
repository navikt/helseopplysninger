package no.nav.helse.hops.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class Vilkarliste(
    val checked: Boolean,
    val id: Int,
    val kreverTilleggsinfo: Boolean = false,
    val tilleggsinfo: String? = null,
    val vilkartekst: String
)
