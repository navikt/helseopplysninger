package no.nav.helse.hops.hops.security.oauth

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.DeserializationFeature
import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import io.ktor.client.HttpClient
import io.ktor.client.engine.java.Java
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import no.nav.security.token.support.client.core.ClientAuthenticationProperties

object OAuth2ClientFactory {
    fun create(
        wellKnownUrl: String,
        clientId: String,
        clientSecret: String
    ): IOAuth2Client {
        val clientAuth = ClientAuthenticationProperties(
            clientId,
            ClientAuthenticationMethod.CLIENT_SECRET_POST,
            clientSecret,
            null,
        )

        return OAuth2Client(createHttpClient(), wellKnownUrl, clientAuth)
    }

    private fun createHttpClient() = HttpClient(Java) {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
                setSerializationInclusion(JsonInclude.Include.NON_NULL)
            }
        }
    }
}
