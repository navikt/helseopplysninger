package dialogmelding.testutils

import no.nav.helse.hops.test.EmbeddedKafka
import no.nav.helse.hops.test.HopsOAuthMock

class Mocks : AutoCloseable {
    val oauth = HopsOAuthMock().apply { start() }
    val converter = ConverterMock()
    val kafka = EmbeddedKafka("helseopplysninger.river")

    override fun close() {
        oauth.shutdown()
        converter.close()
        kafka.close()
    }
}
