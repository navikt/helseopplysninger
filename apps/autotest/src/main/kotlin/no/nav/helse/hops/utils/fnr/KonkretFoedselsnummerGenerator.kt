package no.nav.helse.hops.utils.fnr

import java.time.LocalDate

internal class KonkretFoedselsnummerGenerator(
    private val foedselsdato: LocalDate,
    private val kjoenn: Kjoenn,
    private val dNummer: Boolean
) {
    private var lopeNr: Int = when (kjoenn) {
        Kjoenn.MANN -> 1
        Kjoenn.KVINNE -> 0
    }

    fun next(): FoedselsNr? {
        var fnr: String? = null
        var day = foedselsdato.dayOfMonth
        if (dNummer) day += 40

        while (fnr == null) {
            val individnummer = individnummer(foedselsdato) ?: return null

            fnr = korrigerKontrollsiffer(
                String.format(
                    "%02d%02d%s%03d00",
                    day, foedselsdato.monthValue, foedselsdato.year.toString().takeLast(2), individnummer
                )
            )
        }

        val resultat = FoedselsNr(fnr)
        assert(resultat.foedselsdato == foedselsdato)
        assert(resultat.kjoenn == kjoenn)
        assert(resultat.dNummer == dNummer)
        return resultat
    }

    private fun individnummer(foedselsdato: LocalDate): Int? {
        lopeNr += 2

        for ((individSerie, aarSerie) in FoedselsNr.Companion.tabeller.serier) {
            if (aarSerie.contains(foedselsdato.year)) {
                val kandidat = individSerie.start + lopeNr
                if (!individSerie.contains(kandidat)) {
                    return null // alle individnummer er oppbrukt
                }
                return kandidat
            }
        }
        throw IllegalArgumentException("Fødselsdato må være mellom år 1854 og 2039")
    }

    fun korrigerKontrollsiffer(fnrMedFeilKontrollsiffer: String): String? {
        require(fnrMedFeilKontrollsiffer.length == 11) { "Fødselsnummer må være 11 siffer" }

        var fnr = fnrMedFeilKontrollsiffer.take(9)

        val k1 = FoedselsNr.checksum(FoedselsNr.Companion.tabeller.kontrollsiffer1, fnr)
        fnr += k1

        val k2 = FoedselsNr.checksum(FoedselsNr.Companion.tabeller.kontrollsiffer2, fnr)
        fnr += k2

        if (k1 >= 10 || k2 >= 10) {
            return null
        }

        return fnr
    }
}
