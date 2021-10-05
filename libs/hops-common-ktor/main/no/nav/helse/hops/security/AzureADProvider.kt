package no.nav.helse.hops.security

import io.ktor.application.ApplicationCall
import no.nav.security.token.support.core.jwt.JwtTokenClaims

class AzureADProvider(private val config: IssuerConfig) : AuthProvider {
    companion object {
        const val REALM = "AzureAD"
    }
    override fun extractAuthIdentity(claims: JwtTokenClaims): HopsAuth.AuthIdentity {
        return AzureADIdentity()
    }

    override val issuerName: String
        get() = config.name
    override val applicationConfig: IssuerConfig
        get() = config
    override val realms: List<AuthProvider.Realm>
        get() = listOf(AuthProvider.Realm(REALM))

    class AzureADIdentity : HopsAuth.AuthIdentity
}

fun ApplicationCall.authIdentityAzure(): AzureADProvider.AzureADIdentity = authIdentity() as AzureADProvider.AzureADIdentity
