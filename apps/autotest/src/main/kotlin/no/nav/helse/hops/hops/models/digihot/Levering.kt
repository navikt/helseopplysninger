package no.nav.helse.hops.hops.models.digihot

import kotlinx.serialization.Serializable

@Serializable
data class Levering(
    val hmfArbeidssted: String,
    val hmfEpost: String,
    val hmfEtternavn: String,
    val hmfFornavn: String,
    val hmfPostadresse: String,
    val hmfPostnr: String,
    val hmfPoststed: String,
    val hmfStilling: String,
    val hmfTelefon: String,
    val hmfTreffesEnklest: String,
    val merknadTilUtlevering: String,
    val opfAnsvarFor: String,
    val opfArbeidssted: String,
    val opfEtternavn: String,
    val opfFornavn: String,
    val opfRadioButton: String,
    val opfStilling: String,
    val opfTelefon: String,
    val utleveringEtternavn: String,
    val utleveringFornavn: String,
    val utleveringPostadresse: String,
    val utleveringPostnr: String,
    val utleveringPoststed: String,
    val utleveringTelefon: String,
    val utleveringskontaktpersonRadioButton: String,
    val utleveringsmaateRadioButton: String
)
