package no.nav.helse.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class Brukersituasjon(
    val bostedRadioButton: String,
    val bruksarenaErDagliglivet: Boolean,
    val nedsattFunksjon: Boolean,
    val nedsattFunksjonTypes: NedsattFunksjonTypes,
    val praktiskeProblem: Boolean,
    val storreBehov: Boolean
)
