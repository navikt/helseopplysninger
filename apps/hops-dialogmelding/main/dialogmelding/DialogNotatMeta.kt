package dialogmelding

import no.nav.helse.hops.domain.HerId
import no.nav.helse.hops.domain.HprNr
import no.nav.helse.hops.domain.PersonId
import java.util.Date
import java.util.UUID

class DialogNotatMeta(
    val id: UUID,
    val timestamp: Date,
    val mottaker: Mottaker,
    val pasient: Pasient,
    val behandler: Behandler,
    val pdf: ByteArray,
) {
    class Mottaker(
        val herId: HerId,
        val navn: String,
    )

    class Behandler(
        val hprId: HprNr,
    )

    class Pasient(
        val pid: PersonId,
    )
}
