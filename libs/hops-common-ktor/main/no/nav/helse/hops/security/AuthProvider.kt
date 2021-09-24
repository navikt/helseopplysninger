package no.nav.helse.hops.security

import no.nav.security.token.support.core.context.TokenValidationContext
import no.nav.security.token.support.core.jwt.JwtTokenClaims
import no.nav.security.token.support.ktor.RequiredClaims

interface AuthProvider {
    fun extractAuthIdentity(claims: JwtTokenClaims): HopsAuth.AuthIdentity

    val issuerName: String
    val applicationConfig: IssuerConfig
    val realms: List<Realm>

    class Realm(
        val name: String,
        val requiredClaims: RequiredClaims? = null,
        val additionalValidation: ((TokenValidationContext) -> Boolean)? = null
    )
}
