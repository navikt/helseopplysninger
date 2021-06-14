package no.nav.helse.hops.infrastructure

import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.providers.BearerTokens
import io.ktor.client.features.auth.providers.bearer
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory

object HttpClientFactory {
    fun create(config: Configuration.EventStore) =
        HttpClient {
            val oauth2Client = OAuth2ClientFactory.create(
                config.discoveryUrl.toString(), config.clientId, config.clientSecret
            )

            install(Auth) {
                bearer {
                    loadTokens {
                        val token = oauth2Client.getToken(config.scope)
                        BearerTokens(token, "")
                    }
                }
            }
        }
}
