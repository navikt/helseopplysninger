package no.nav.helse.hops.security

import io.ktor.application.ApplicationCall
import no.nav.security.token.support.core.jwt.JwtTokenClaims

class MaskinportenProvider(private val config: Configuration) : AuthProvider {
    data class Configuration(
        val issuer: IssuerConfig,
        val readScope: String,
        val writeScope: String
    )

    companion object {
        const val READ_REALM = "MaskinportenRead"
        const val WRITE_REALM = "MaskinportenWrite"
    }

    override fun extractAuthIdentity(claims: JwtTokenClaims): HopsAuth.AuthIdentity {
        val consumer = claims["consumer"] as Map<*, *>
        val orgNr = consumer["ID"] as String

        return MaskinportenIdentity(orgNr.substringAfter(":"))
    }

    override val issuerName: String
        get() = config.issuer.name

    override val applicationConfig: IssuerConfig
        get() = config.issuer

    override val realms: List<AuthProvider.Realm>
        get() = listOf(
            AuthProvider.Realm(READ_REALM, additionalValidation = {
                val scopes: List<String> = it.getClaims(issuerName)
                    .getStringClaim("scope")
                    ?.split(" ")
                    ?: emptyList()
                scopes.contains(config.readScope)
            }),
            AuthProvider.Realm(WRITE_REALM, additionalValidation = {
                val scopes: List<String> = it.getClaims(issuerName)
                    .getStringClaim("scope")
                    ?.split(" ")
                    ?: emptyList()
                scopes.contains(config.writeScope)
            })
        )

    data class MaskinportenIdentity(val orgNr: String) : HopsAuth.AuthIdentity
}

fun ApplicationCall.authIdentityMaskinporten(): MaskinportenProvider.MaskinportenIdentity =
    authIdentity() as MaskinportenProvider.MaskinportenIdentity
