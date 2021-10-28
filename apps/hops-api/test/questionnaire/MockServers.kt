package questionnaire

import io.ktor.http.HttpHeaders
import io.ktor.http.withCharset
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.test.HopsOAuthMock
import no.nav.helse.hops.test.MockServer
import okhttp3.mockwebserver.MockResponse
import org.junit.jupiter.api.extension.BeforeAllCallback
import org.junit.jupiter.api.extension.ExtensionContext
import java.util.concurrent.atomic.AtomicBoolean

object MockServers {
    val oAuth = HopsOAuthMock()

    val eventStore = MockServer().apply {
        matchRequest(
            { request -> request.method == "GET" && request.path == "/fhir/4.0/Bundle" },
            {
                MockResponse()
                    .setHeader(HttpHeaders.ContentType, ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString())
                    .setBody("""{"resourceType": "Bundle"}""")
            }
        )
        matchRequest(
            { request -> request.method == "POST" && request.path == "/fhir/4.0/\$process-message" },
            {
                MockResponse()
                    .setHeader(HttpHeaders.ContentType, ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString())
                    .setBody("""""")
            }
        )
    }

    object Setup : BeforeAllCallback {
        private val started = AtomicBoolean(false)
        override fun beforeAll(context: ExtensionContext?) {
            if (!started.getAndSet(true)) {
                oAuth.start()
                eventStore.start()
            }
        }
    }
}
