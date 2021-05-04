package no.nav.helse.hops.auth

import com.nimbusds.jwt.JWTClaimsSet
import com.nimbusds.jwt.JWTParser
import no.nav.helse.hops.security.oauth.IOAuth2Client
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory
import java.util.*

class Auth {
    val client = initClient();
    private var _token: JWTClaimsSet? = null;
    private fun initClient(): IOAuth2Client {
        return OAuth2ClientFactory.create(
            "http://localhost:8081/default/.well-known/openid-configuration",
            "wouldBeIdentifing",
            "wouldBeSecret"
        );
    }


    suspend fun token(): JWTClaimsSet? {
        if (_token == null || _token!!.expirationTime > Date()) {
            val rawToken = client.getToken("myscope")
            _token = JWTParser.parse(rawToken).jwtClaimsSet
        }
        return _token
    }
}
