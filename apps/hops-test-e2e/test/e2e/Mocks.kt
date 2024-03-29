package e2e

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import e2e.Mocks.Dispatcher.respond
import e2e.Mocks.Matcher.get
import e2e.Mocks.Matcher.post
import e2e.Mocks.Testdata.maskinportResponse
import e2e.fhir.FhirResource
import no.nav.helse.hops.convert.ContentTypes.fhirJsonR4
import no.nav.helse.hops.maskinporten.GRANT_TYPE
import no.nav.helse.hops.test.EmbeddedKafka
import no.nav.helse.hops.test.MockServer
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.intellij.lang.annotations.Language
import java.util.Date

const val LIVENESS_PATH = "/actuator/live"

object Mocks {
    val maskinporten = MockServer().apply {
        matchRequest(
            post(
                path = "/token",
                header = "Content-Type" to "application/x-www-form-urlencoded",
                containsBody = "grant_type=$GRANT_TYPE&assertion="
            ),
            respond(
                body = maskinportResponse,
                headers = headersOf("Content-Type", "application/json")
            )
        )
    }

    val api = MockServer().apply {
        matchRequest(get(LIVENESS_PATH), respond("live"))
        matchRequest(get("/fhir/4.0/Bundle", "accept" to fhirJsonR4.toString()), respond("{}"))
        matchRequest(post("/fhir/4.0/\$process-message")) {

            // Most recent content is most likely correct (we don't know the resourceId)
            val content = FhirResource.get { true }.maxByOrNull { it.timestamp }!!

            // Simulate hops-event-replay-kafka and put the message on kafka.
            kafka.produce(
                topic = "helseopplysninger.river",
                key = content.id,
                value = content.json.toByteArray()
            )

            // Then respond with Accepted
            MockResponse().setResponseCode(202)
        }
    }

    val eventreplay = MockServer().apply {
        matchRequest(get(LIVENESS_PATH), respond("live"))
    }

    val eventsink = MockServer().apply {
        matchRequest(get(LIVENESS_PATH), respond("live"))
    }

    val eventstore = MockServer().apply {
        matchRequest(get(LIVENESS_PATH), respond("live"))
    }

    lateinit var kafka: EmbeddedKafka

    object Matcher {
        fun get(path: String) = { req: RecordedRequest ->
            req.method == "GET" && req.hasPath(path)
        }

        fun get(path: String, header: Pair<String, String>) = { req: RecordedRequest ->
            req.method == "GET" && req.hasPath(path) && req.hasHeader(header)
        }

        fun post(path: String) = { req: RecordedRequest ->
            req.method == "POST" && req.hasPath(path)
        }

        fun post(path: String, header: Pair<String, String>, containsBody: String) = { req: RecordedRequest ->
            req.method == "POST" && req.hasPath(path) && req.hasHeader(header) && req.containsBody(containsBody)
        }

        private fun RecordedRequest.hasPath(pathString: String) = path!!.startsWith(pathString)
        private fun RecordedRequest.containsBody(content: String) = body.readUtf8().startsWith(content)
        private fun RecordedRequest.hasHeader(header: Pair<String, String>) = getHeader(header.first) == header.second
    }

    object Dispatcher {
        fun respond(code: Int = 200) = { _: RecordedRequest ->
            MockResponse().setResponseCode(code)
        }

        fun respond(body: String = "", code: Int = 200) = { _: RecordedRequest ->
            MockResponse().setBody(body).setResponseCode(code)
        }

        fun respond(body: String = "", headers: Headers, code: Int = 200) = { _: RecordedRequest ->
            MockResponse().setHeaders(headers).setBody(body).setResponseCode(code)
        }
    }

    object Testdata {
        @Language("json")
        val maskinportResponse =
            """
            {
              "access_token" : "$maskinportenToken",
              "expires_in" : 120,
              "scope" : "nav:helse:helseopplysninger.read nav:helse:helseopplysninger.write",
              "token_type": "jwt"
            }
            """.trimIndent()

        private val maskinportenToken: String
            get() = RSAKeyGenerator(2048).keyID("123").generate().let { privateKey ->
                val header = JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.keyID).build()
                val exp = Date(Date().time + (60 * 1000))
                val claims = JWTClaimsSet.Builder().subject("test").issuer("e2e").expirationTime(exp).build()
                SignedJWT(header, claims).apply { sign(RSASSASigner(privateKey)) }.serialize()
            }
    }
}
