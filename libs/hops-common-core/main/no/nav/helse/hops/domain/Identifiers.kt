package no.nav.helse.hops.domain

/** Fødselsnummer eller D-nummer. **/
@JvmInline
value class PersonId(private val value: String) {
    init {
        require(pattern.matches(value)) {
            "$value is not a valid Norwegian Personal Identifier according to regex: $pattern"
        }
    }

    val type get() = if (value.first().digitToInt() >= 4) Type.DNR else Type.FNR
    val system get() = if (type == Type.FNR) fnrSystem else dnrSystem
    val gender get() = if (value[8].digitToInt() % 2 == 0) Gender.Female else Gender.Male
    override fun toString() = value

    enum class Type { FNR, DNR }
    enum class Gender { Female, Male }

    companion object {
        private val pattern = Regex("""^[0-7]\d[0-1]\d\d{7}$""")
        val fnrSystem = "urn:oid:2.16.578.1.12.4.1.4.1"
        val dnrSystem = "urn:oid:2.16.578.1.12.4.1.4.2"
    }
}

/** Helsepersonellnummer er en unik id i Helsepersonellregisteret som forvaltes av Helsedirektoratet. */
@JvmInline
value class HprNr(private val value: String) {
    init {
        require(pattern.matches(value)) {
            "$value is not a valid HPR-number according to regex: $pattern"
        }
    }

    override fun toString() = value

    companion object {
        private val pattern = Regex("""^\d{1,9}$""")
        val system = "urn:oid:2.16.578.1.12.4.1.4.4"
    }
}

/** Organisasjonsnummer. */
@JvmInline
value class OrgNr(private val value: String) {
    init {
        require(pattern.matches(value)) {
            "$value is not a valid Org-NR according to regex: $pattern"
        }
    }

    override fun toString() = value

    companion object {
        private val pattern = Regex("""^\d{9}$""")
        val system = "urn:oid:2.16.578.1.12.4.1.4.101"
    }
}

/**
 * HER-id er en unik identifikator av en kommunikasjonspart.
 * HER-id tildeles av Norsk Helsenett når kommunikasjonsparten blir registrert i NHN Adresseregister.
 */
@JvmInline
value class HerId(private val value: String) {
    init {
        require(pattern.matches(value)) {
            "$value is not a valid HER-id according to regex: $pattern"
        }
    }

    override fun toString() = value

    companion object {
        private val pattern = Regex("""^\d+$""")
        val system = "urn:oid:2.16.578.1.12.4.1.2"
    }
}
