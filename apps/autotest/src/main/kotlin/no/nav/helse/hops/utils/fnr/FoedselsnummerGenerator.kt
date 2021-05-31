package no.nav.helse.hops.utils.fnr

import java.lang.IllegalStateException
import java.time.LocalDate

class FoedselsnummerGenerator {

    private val generators: MutableMap<Params, KonkretFoedselsnummerGenerator> = mutableMapOf()

    private var date: LocalDate = LocalDate.of(1854, 1, 1)

    fun foedselsnummer(
        foedselsdato: LocalDate? = null,
        kjoenn: Kjoenn = Kjoenn.MANN,
        dNummer: Boolean = false
    ): FoedselsNr {
        if (foedselsdato != null) {
            val fnr = generator(foedselsdato, kjoenn, dNummer).next()
            return fnr ?: throw IllegalStateException("Tom for fødselsnummer på den aktuelle datoen")
        }

        var fnr: FoedselsNr? = null
        while (fnr == null) {
            fnr = generator(date, kjoenn, dNummer).next()
            if (fnr == null) {
                date = date.plusDays(1)
            }
        }
        return fnr
    }

    private fun generator(
        foedselsdato: LocalDate,
        kjoenn: Kjoenn,
        dNummer: Boolean
    ): KonkretFoedselsnummerGenerator {
        val params = Params(foedselsdato, kjoenn, dNummer)
        val generator = generators.getOrElse(params) {
            KonkretFoedselsnummerGenerator(foedselsdato, kjoenn, dNummer)
        }
        generators[params] = generator
        return generator
    }

    private data class Params(val foedselsdato: LocalDate, val kjoenn: Kjoenn, val dNummer: Boolean)
}
