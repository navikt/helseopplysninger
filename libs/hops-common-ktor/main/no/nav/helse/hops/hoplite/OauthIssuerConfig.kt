package no.nav.helse.hops.hoplite

import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import java.net.URL

data class OauthIssuerConfig(
    val discoveryUrl: URL,
    val name: String = discoveryUrl.toString().substringBefore(".well-known/"),
    val audience: String,
    val optionalClaims: String?
)

/** Converts to the format expected by the
 * [token-support][https://github.com/navikt/token-support#required-properties-yaml-or-properties] library. */
fun List<OauthIssuerConfig>.asApplicationConfig(): ApplicationConfig = MapApplicationConfig().apply {
    val prefix = "no.nav.security.jwt.issuers"
    put("$prefix.size", count().toString())

    forEachIndexed { i, issuer ->
        put("$prefix.$i.issuer_name", issuer.name)
        put("$prefix.$i.discoveryurl", issuer.discoveryUrl.toString())
        put("$prefix.$i.accepted_audience", issuer.audience)
        issuer.optionalClaims?.let {
            put("$prefix.$i.validation.optional-claims", it)
        }
    }
}
