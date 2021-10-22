package archive

import java.io.Closeable
import no.nav.helse.hops.test.HopsOAuthMock

class Mocks : Closeable {
    val oauth = HopsOAuthMock().apply { start() }
    val dokarkiv = DokarkivMock()
    val converter = ConverterMock()
    val kafka = EmbeddedKafka().apply { start() }

    override fun close() {
        oauth.shutdown()
        kafka.shutdown()
        converter.close()
        dokarkiv.close()
    }
}