package api.infrastructure

import io.ktor.auth.Authentication
import no.nav.helse.hops.hoplite.asApplicationConfig
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.tokenValidationSupport

object Constants {
    const val PUBLISH = "publish"
    const val SUBSCRIBE = "subscribe"
}

fun Authentication.Configuration.useNaviktTokenSupport(config: Config.ModuleOAuth) {
    val pubScope = config.publishScope
    val subScope = config.subscribeScope

    // Workaround because Token-Support does not handle scope-claim with multiple values.
    val unionClaims = arrayOf("scope=$pubScope $subScope", "scope=$subScope $pubScope")
    val pubClaims = unionClaims + "scope=$pubScope"
    val subClaims = unionClaims + "scope=$subScope"

    val issuer = config.maskinporten.name
    val pubReq = RequiredClaims(issuer, pubClaims, true)
    val subReq = RequiredClaims(issuer, subClaims, true)

    val ktorConfig = listOf(config.maskinporten).asApplicationConfig()
    tokenValidationSupport(Constants.PUBLISH, ktorConfig, pubReq)
    tokenValidationSupport(Constants.SUBSCRIBE, ktorConfig, subReq)
}
