package no.nav.helse.hops.test

import com.nimbusds.jwt.SignedJWT
import no.nav.helse.hops.security.HopsAuth
import no.nav.security.mock.oauth2.MockOAuth2Server

public class HopsOAuthMock {

    companion object {
        const val MP_SCOPE_READ = "nav:helse:helseopplysninger.read"
        const val MP_SCOPE_WRITE = "nav:helse:helseopplysninger.write"

        const val MASKINPORTEN_ISSUER_NAME = "maskinporten"
        const val AZURE_ISSUER_NAME = "azure"
    }

    enum class MaskinportenScopes(val value: String) {
        READ("nav:helse:helseopplysninger.read"),
        WRITE("nav:helse:helseopplysninger.write")
    }

    private val server = MockOAuth2Server()

    fun maskinportenWellKnownUrl() = server.wellKnownUrl(MASKINPORTEN_ISSUER_NAME)

    fun azureWellKnownUrl() = server.wellKnownUrl(AZURE_ISSUER_NAME)

    fun issueMaskinportenToken(
        orgNumber: String = "889640782",
        scopes: Set<MaskinportenScopes> = setOf(MaskinportenScopes.READ, MaskinportenScopes.WRITE)
    ): SignedJWT =
        server.issueToken(
            issuerId = MASKINPORTEN_ISSUER_NAME,
            claims = mapOf(
                "scope" to scopes.joinToString(" ") { it.value },
                "consumer" to mapOf(
                    "authority" to "iso6523-actorid-upis",
                    "ID" to "0192:$orgNumber"
                )
            )
        )

    // Add claims as required: https://doc.nais.io/security/auth/azure-ad/configuration/#claims
    fun issueAzureToken(): SignedJWT = server.issueToken(issuerId = AZURE_ISSUER_NAME)
    fun start() = server.start()
    fun shutdown() = server.shutdown()

    fun buildMaskinportenConfig() = HopsAuth.Configuration.Maskinporten(
        issuer = HopsAuth.Configuration.IssuerConfig(
            name = MASKINPORTEN_ISSUER_NAME,
            discoveryUrl = maskinportenWellKnownUrl().toUrl(),
            audience = "default",
            optionalClaims = null
        ),
        readScope = MaskinportenScopes.READ.value,
        writeScope = MaskinportenScopes.WRITE.value
    )

    fun buildAzureConfig() = HopsAuth.Configuration.IssuerConfig(
        name = AZURE_ISSUER_NAME,
        discoveryUrl = azureWellKnownUrl().toUrl(),
        audience = "default",
        optionalClaims = null
    )
}
