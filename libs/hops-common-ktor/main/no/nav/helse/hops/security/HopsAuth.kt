package no.nav.helse.hops.security

import io.ktor.application.Application
import io.ktor.application.ApplicationCall
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.Authentication
import io.ktor.auth.authentication
import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import io.ktor.util.AttributeKey
import java.net.URL
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import no.nav.security.token.support.ktor.tokenValidationSupport

class HopsAuth(configuration: Configuration) {
    internal val maskinporten = configuration.maskinporten // Snapshot of the mutable config into an immutable property
    internal val azureAD = configuration.azureAD

    init {
        if (maskinporten == null && azureAD == null) {
            throw IllegalArgumentException("No authentication configuration provided!")
        }
    }

    class Configuration {
        var maskinporten: Maskinporten? = null
        var azureAD: IssuerConfig? = null

        data class Maskinporten(
            val issuer: IssuerConfig,
            val readScope: String,
            val writeScope: String
        )

        data class IssuerConfig(
            val name: String,
            val discoveryUrl: URL,
            val audience: String,
            val optionalClaims: String?
        )
    }

    abstract class Realms<T>(val name: String) {
        internal abstract fun requireClaims(config: T): RequiredClaims

        companion object {
            val maskinportenRead = object : Realms<Configuration.Maskinporten>("MaskinportenRead") {
                override fun requireClaims(config: Configuration.Maskinporten): RequiredClaims {
                    val unionClaims = unionClaims(config)
                    val claims = unionClaims + "scope=${config.readScope}"
                    return RequiredClaims(config.issuer.name, claims, true)
                }
            }
            val maskinportenWrite = object : Realms<Configuration.Maskinporten>("MaskinportenWrite") {
                override fun requireClaims(config: Configuration.Maskinporten): RequiredClaims {
                    val unionClaims = unionClaims(config)
                    val claims = unionClaims + "scope=${config.writeScope}"
                    return RequiredClaims(config.issuer.name, claims, true)
                }
            }
            val azureAd = object : Realms<Configuration.IssuerConfig>("AzureAD") {
                override fun requireClaims(config: Configuration.IssuerConfig): RequiredClaims {
                    return RequiredClaims(config.name, emptyArray(), true)
                }
            }
        }

        protected fun unionClaims(config: Configuration.Maskinporten): Array<String> {
            val writeScope = config.writeScope
            val readScope = config.readScope
            return arrayOf("scope=$writeScope $readScope", "scope=$readScope $writeScope")
        }
    }

    sealed class AuthIdentity {
        class AzureAD : AuthIdentity()
        data class Maskinporten(val orgNr: String) : AuthIdentity()
    }

    companion object Feature : ApplicationFeature<Application, Configuration, HopsAuth> {
        override val key = AttributeKey<HopsAuth>("HopsAuth")

        override fun install(pipeline: Application, configure: Configuration.() -> Unit): HopsAuth {
            val configuration = Configuration().apply(configure)
            val feature = HopsAuth(configuration)

            pipeline.install(Authentication) {
                feature.configureAuthProviders(this)
            }
            pipeline.intercept(ApplicationCallPipeline.Call) {
                call.attributes.put(key, feature)
            }
            return feature
        }
    }

    private fun configureAuthProviders(authConfigurer: Authentication.Configuration) {
        maskinporten?.let {
            val applicationConfig = listOf(it.issuer).asApplicationConfig()
            authConfigurer.tokenValidationSupport(
                Realms.maskinportenWrite.name,
                applicationConfig,
                Realms.maskinportenWrite.requireClaims(it)
            )
            authConfigurer.tokenValidationSupport(
                Realms.maskinportenRead.name,
                applicationConfig,
                Realms.maskinportenRead.requireClaims(it)
            )
        }
        azureAD?.let {
            val applicationConfig = listOf(it).asApplicationConfig()
            authConfigurer.tokenValidationSupport(
                Realms.azureAd.name,
                applicationConfig,
                Realms.azureAd.requireClaims(it)
            )
            if (maskinporten == null) {
                // configure default provider
                authConfigurer.tokenValidationSupport(config = applicationConfig)
            }
        }
    }

    /** Converts to the format expected by the
     * [token-support][https://github.com/navikt/token-support#required-properties-yaml-or-properties] library. */
    private fun List<Configuration.IssuerConfig>.asApplicationConfig(): ApplicationConfig =
        MapApplicationConfig().apply {
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
}

fun ApplicationCall.authIdentity(): HopsAuth.AuthIdentity {
    val principal = authentication.principal<TokenValidationContextPrincipal>()
        ?: throw IllegalStateException("There is no Principal present, make sure you are in an authenticated route")

    val feature = attributes[HopsAuth.key]
    feature.maskinporten?.let {
        if (principal.context.hasTokenFor(it.issuer.name)) {
            val claims = principal.context.getClaims(it.issuer.name)
            val consumer = claims["consumer"] as Map<*, *>
            val orgNr = consumer["ID"] as String

            return HopsAuth.AuthIdentity.Maskinporten(orgNr.substringAfter(":"))
        }
    }
    feature.azureAD?.let {
        if (principal.context.hasTokenFor(it.name)) {
            return HopsAuth.AuthIdentity.AzureAD()
        }
    }

    error("No tokens found for neither AzureAD nor Maskinporten!")
}

fun ApplicationCall.authIdentityAzure(): HopsAuth.AuthIdentity.AzureAD = authIdentity() as HopsAuth.AuthIdentity.AzureAD
fun ApplicationCall.authIdentityMaskinporten(): HopsAuth.AuthIdentity.Maskinporten =
    authIdentity() as HopsAuth.AuthIdentity.Maskinporten
