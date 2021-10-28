package dialogmelding

data class Melding(
    val mottaker: Mottaker,
    val pasient: Pasient,
    val behandler: Behandler,
    val vedlegg: ByteArray,
) {
    data class Mottaker(
        val partnerId: String?,
        val herId: String?,
        val orgnummer: String?,
        val navn: String?,
        val adresse: String?,
        val postnummer: String?,
        val poststed: String?
    )

    data class Behandler(
        val fnr: String?,
        val hprId: String?,
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
    )

    data class Pasient(
        val fnr: String?,
        val fornavn: String?,
        val mellomnavn: String?,
        val etternavn: String?,
    )
}
