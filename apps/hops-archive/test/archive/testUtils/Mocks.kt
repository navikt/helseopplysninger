package archive.testUtils

import no.nav.helse.hops.test.HopsOAuthMock

class Mocks : AutoCloseable {
    val oauth = HopsOAuthMock().apply { start() }
    val dokarkiv = DokarkivMock()
    val converter = ConverterMock()
    val kafka = EmbeddedKafka()

    override fun close() {
        oauth.shutdown()
        kafka.close()
        converter.close()
        dokarkiv.close()
    }
}