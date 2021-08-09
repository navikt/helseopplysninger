package no.nav.helse.hops.hops.auth

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import no.nav.helse.hops.hops.security.oauth.IOAuth2Client
import no.nav.helse.hops.hops.security.oauth.OAuth2ClientFactory
import no.nav.helse.hops.hops.utils.DockerComposeEnv
import java.util.Date

class Auth {
    private val client = initClient()
    private var _token: JWTClaimsSet? = null

    private fun initClient(): IOAuth2Client {
        return OAuth2ClientFactory.create(
            DockerComposeEnv().discoveryUrl,
            "wouldBeIdentifing",
            "wouldBeSecret"
        )
    }

    suspend fun token(scope: String): JWTClaimsSet? {
        if (_token == null || _token!!.expirationTime > Date()) {
            val rawToken = client.getToken(scope)
            _token = JWTParser.parse(rawToken).jwtClaimsSet
        }
        return _token
    }
}
