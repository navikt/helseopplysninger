package e2e

import com.nimbusds.jose.JWSAlgorithm
import com.nimbusds.jose.JWSHeader
import com.nimbusds.jose.JWSSigner
import com.nimbusds.jose.crypto.RSASSASigner
import com.nimbusds.jose.jwk.RSAKey
import com.nimbusds.jose.jwk.gen.RSAKeyGenerator
import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.SignedJWT
import e2e.Mocks.Dispatcher.respond
import e2e.Mocks.Matcher.get
import e2e.Mocks.Matcher.post
import no.nav.helse.hops.convert.ContentTypes.fhirJsonR4
import no.nav.helse.hops.mocks.MockServer
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest
import org.intellij.lang.annotations.Language
import java.util.Date

internal const val GRANT_TYPE = "urn:ietf:params:oauth:grant-type:jwt-bearer"

object Mocks {
    val maskinporten = MockServer().apply {
        matchRequest(
            post(
                path = "/token",
                header = "Content-Type" to "application/x-www-form-urlencoded",
                body = "grant_type=$GRANT_TYPE&assertion="
            ),
            respond(Testdata.maskinportResponse)
        )
    }

    val api = MockServer().apply {
        matchRequest(get("/isAlive"), respond("live"))
        matchRequest(post("/fhir/4.0/\$process-message"), respond())
        matchRequest(get("/fhir/4.0/Bundle", "accept" to fhirJsonR4.toString()), respond("e2e"))
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
        fun get(path: String, header: Pair<String, String>? = null) = { req: RecordedRequest ->
            req.method == "GET" && req.hasPath(path) && req.hasHeader(header)
        }

        fun post(path: String, header: Pair<String, String>? = null, body: String? = null) = { req: RecordedRequest ->
            req.method == "POST" && req.hasPath(path) && req.hasHeader(header) && req.containsBody(body)
        }

        private fun RecordedRequest.hasPath(path: String) =
            this.path?.startsWith(path) ?: false

        private fun RecordedRequest.containsBody(body: String?) =
            body?.let { this.body.readUtf8().startsWith(it) } ?: true

        private fun RecordedRequest.hasHeader(header: Pair<String, String>?) =
            header?.let { (key, value) -> this.getHeader(key) == value } ?: true
    }

    object Dispatcher {
        fun respond(body: String = "", code: Int = 200) = { _: RecordedRequest ->
            MockResponse().setResponseCode(code).setBody(body)
        }
    }

    object Testdata {
        @Language("json")
        val maskinportResponse =
            """
            {
              "access_token" : "${maskinportenToken()}",
              "token_type" : "Bearer",
              "expires_in" : 599,
              "scope" : "nav:helse:helseopplysninger.read"
            }
            """.trimIndent()

        private fun maskinportenToken(): String {
            val privateKey: RSAKey = RSAKeyGenerator(2048).keyID("123").generate()

            val claimsSet = JWTClaimsSet.Builder()
                .subject("alice")
                .issuer("https://c2id.com")
                .expirationTime(Date(Date().time + (60 * 1000)))
                .build()

            val signedJWT = SignedJWT(JWSHeader.Builder(JWSAlgorithm.RS256).keyID(privateKey.keyID).build(), claimsSet)
            val signer: JWSSigner = RSASSASigner(privateKey)
            signedJWT.sign(signer)
            return signedJWT.serialize()
        }
    }
}
