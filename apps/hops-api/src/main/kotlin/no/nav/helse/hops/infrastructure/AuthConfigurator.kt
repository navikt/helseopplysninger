package no.nav.helse.hops.infrastructure

import io.ktor.auth.Authentication
import io.ktor.config.ApplicationConfig
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.tokenValidationSupport

object Constants {
    const val PUBLISH = "publish"
    const val SUBSCRIBE = "subscribe"
}

fun Authentication.Configuration.useNaviktTokenSupport(config: ApplicationConfig) {
    val pubScope = config.property("security.scopes.publish").getString()
    val subScope = config.property("security.scopes.subscribe").getString()

    // Workaround because Token-Support does not handle scope-claim with multiple values.
    val unionClaims = arrayOf("scope=$pubScope $subScope", "scope=$subScope $pubScope")
    val pubClaims = unionClaims + "scope=$pubScope"
    val subClaims = unionClaims + "scope=$subScope"

    val issuer = config
        .configList("no.nav.security.jwt.issuers")[0]
        .property("issuer_name")
        .getString()

    val pubReq = RequiredClaims(issuer, pubClaims, true)
    val subReq = RequiredClaims(issuer, subClaims, true)

    tokenValidationSupport(Constants.PUBLISH, config, pubReq)
    tokenValidationSupport(Constants.SUBSCRIBE, config, subReq)
}
