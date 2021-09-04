package no.nav.helse.hops.hoplite

import com.sksamuel.hoplite.ConfigAlias
import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import java.net.URL

const val prefix = "no.nav.security.jwt.issuers"

/** [See][https://github.com/navikt/token-support#required-properties-yaml-or-properties]
 * for documentation and possible properties. */
data class NavTokenSupportConfig(
    @ConfigAlias(prefix) val issuers: List<OauthIssuer>) {
    data class OauthIssuer(
        @ConfigAlias("issuer_name") val name: String,
        val discoveryUrl: URL,
        @ConfigAlias("accepted_audience") val audience: String,
        @ConfigAlias("validation.optional-claims") val optionalClaims: String?
    )

    fun asApplicationConfig(): ApplicationConfig = MapApplicationConfig().apply {
        put("$prefix.size", issuers.count().toString())

        issuers.forEachIndexed { i, issuer ->
            put("$prefix.$i.issuer_name", issuer.name)
            put("$prefix.$i.discoveryUrl", issuer.discoveryUrl.toString())
            put("$prefix.$i.accepted_audience", issuer.audience)
            issuer.optionalClaims?.let {
                put("$prefix.$i.validation.optional-claims", it)
            }
        }
    }
}
