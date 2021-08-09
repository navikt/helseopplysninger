package no.nav.helse.hops.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class HjelpemiddelListe(
    val antall: Int,
    val begrunnelsen: String? = null,
    val beskrivelse: String,
    val hjelpemiddelkategori: String,
    val hmsNr: String,
    val kanIkkeTilsvarande: Boolean,
    val navn: String? = null,
    val produkt: Produkt,
    val tilbehorListe: List<TilbehorListe>? = listOf(),
    val tilleggsinformasjon: String,
    val uniqueKey: String,
    val utlevertFraHjelpemiddelsentralen: Boolean,
    val vilkarliste: List<Vilkarliste>? = listOf(),
    val vilkaroverskrift: String? = null
)
