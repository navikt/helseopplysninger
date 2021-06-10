package no.nav.helse.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class NedsattFunksjonTypes(
    val bevegelse: Boolean,
    val horsel: Boolean,
    val kognisjon: Boolean
)
