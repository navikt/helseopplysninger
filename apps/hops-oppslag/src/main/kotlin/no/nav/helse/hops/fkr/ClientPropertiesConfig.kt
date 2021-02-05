package no.nav.helse.hops.fkr

import com.nimbusds.oauth2.sdk.auth.ClientAuthenticationMethod
import io.ktor.config.*
import no.nav.security.token.support.client.core.ClientAuthenticationProperties
import no.nav.security.token.support.client.core.ClientProperties
import no.nav.security.token.support.client.core.OAuth2GrantType
import java.net.URI

class ClientPropertiesConfig(applicationConfig: ApplicationConfig)
{
    val clientConfig: Map<String, ClientProperties> =
        applicationConfig.configList(CLIENTS_PATH)
            .associate { clientConfig ->

                val wellKnownUrl = clientConfig.propertyToStringOrNull("well_known_url")
                val resourceUrl = clientConfig.propertyToStringOrNull("resource_url")

                clientConfig.propertyToString(CLIENT_NAME) to ClientProperties(
                    URI(clientConfig.propertyToString("token_endpoint_url")),
                    wellKnownUrl?.let { URI(it) },
                    OAuth2GrantType(clientConfig.propertyToString("grant_type")),
                    clientConfig.propertyToStringOrNull("scope")?.split(","),
                    ClientAuthenticationProperties(
                        clientConfig.propertyToString("authentication.client_id"),
                        ClientAuthenticationMethod(
                            ClientAuthenticationMethod.CLIENT_SECRET_POST.value
                        ),
                        clientConfig.propertyToStringOrNull("authentication.client_secret"),
                        null // N/A
                    ),
                    resourceUrl?.let { URI(it) },
                    null // N/A
                )
            }

    val cacheConfig: OAuth2Cache =
        with(applicationConfig.config(CACHE_PATH)) {
            OAuth2Cache(
                enabled = propertyToStringOrNull("cache.enabled")?.toBoolean() ?: false,
                maximumSize = propertyToStringOrNull("cache.maximumSize")?.toLong() ?: 0,
                evictSkew = propertyToStringOrNull("cache.evictSkew")?.toLong() ?: 0
            )
        }
}

private const val COMMON_PREFIX = "no.nav.security.jwt.client.registration"
private const val CLIENTS_PATH = "${COMMON_PREFIX}.clients"
private const val CACHE_PATH = "${COMMON_PREFIX}.cache"
private const val CLIENT_NAME = "client_name"

private fun ApplicationConfig.propertyToString(prop: String) = property(prop).getString()
private fun ApplicationConfig.propertyToStringOrNull(prop: String) = propertyOrNull(prop)?.getString()

data class OAuth2Cache(
    val enabled: Boolean,
    val maximumSize: Long,
    val evictSkew: Long
)