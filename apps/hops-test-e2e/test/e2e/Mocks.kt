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
import no.nav.helse.hops.convert.ContentTypes.fhirJsonR4
import no.nav.helse.hops.maskinporten.GRANT_TYPE
import no.nav.helse.hops.test.MockServer
import okhttp3.Headers
import okhttp3.Headers.Companion.headersOf
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.intellij.lang.annotations.Language
import java.util.Date

object Mocks {
    val maskinporten = MockServer().apply {
        matchRequest(
            post(
                path = "/token",
                header = "Content-Type" to "application/x-www-form-urlencoded",
                containsBody = "grant_type=$GRANT_TYPE&assertion="
            ),
            respond(maskinportResponse, headersOf("Content-Type", "application/json"))
        )
    }

    val api = MockServer().apply {
        matchRequest(get("/isAlive"), respond("live"))
        matchRequest(post("/fhir/4.0/\$process-message"), respond(202))
        matchRequest(get("/fhir/4.0/Bundle", "accept" to fhirJsonR4.toString()), respond("{}"))
    }

    val eventreplay = MockServer().apply {
        matchRequest(get("/isAlive"), respond("live"))
    }

    val eventsink = MockServer().apply {
        matchRequest(get("/isAlive"), respond("live"))
    }

    val eventstore = MockServer().apply {
        matchRequest(get("/isAlive"), respond("live"))
    }

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
              "scope" : "nav:helse:helseopplysninger.read nav:helse:helseopplysninger.write"
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
