package no.nav.helse.hops.auth

import io.ktor.application.Application
import io.ktor.application.install
import io.ktor.auth.Authentication
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.tokenValidationSupport

fun Application.configureAuthentication() {
    val config = this.environment.config
    val issuer = config
        .configList("no.nav.security.jwt.issuers")[0]
        .property("issuer_name")
        .getString()

    install(Authentication) {
        tokenValidationSupport(
            config = config,
            requiredClaims = RequiredClaims(
                issuer,
                arrayOf("scope=nav:helse/v1/helseopplysninger")
            )
        )
    }
}
