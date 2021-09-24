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
import no.nav.security.token.support.ktor.TokenValidationContextPrincipal
import no.nav.security.token.support.ktor.tokenValidationSupport

class HopsAuth(configuration: Configuration) {
    internal val providers = configuration.providers.toList()

    init {
        if (providers.isEmpty()) {
            error("No authentication configuration provided!")
        }
    }

    class Configuration {
        val providers = mutableListOf<AuthProvider>()
    }

    /** Converts to the format expected by the
     * [token-support][https://github.com/navikt/token-support#required-properties-yaml-or-properties] library. */
    private fun IssuerConfig.asApplicationConfig(): ApplicationConfig = MapApplicationConfig().apply {
        val prefix = "no.nav.security.jwt.issuers"
        put("$prefix.size", "1")

        put("$prefix.0.issuer_name", name)
        put("$prefix.0.discoveryurl", discoveryUrl.toString())
        put("$prefix.0.accepted_audience", audience)
        optionalClaims?.let {
            put("$prefix.0.validation.optional_claims", it)
        }
    }

    interface AuthIdentity {
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
        providers.forEach { provider ->
            provider.realms.forEach { realm ->
                authConfigurer.tokenValidationSupport(
                    name = realm.name,
                    config = provider.applicationConfig.asApplicationConfig(),
                    requiredClaims = realm.requiredClaims,
                    additionalValidation = realm.additionalValidation
                )
            }
        }
        if (providers.size == 1 && providers[0].realms.size == 1) {
            // configure default provider
            val provider = providers[0]
            val realm = provider.realms[0]
            authConfigurer.tokenValidationSupport(
                config = provider.applicationConfig.asApplicationConfig(),
                requiredClaims = realm.requiredClaims,
                additionalValidation = realm.additionalValidation
            )
        }
    }
}

fun ApplicationCall.authIdentity(): HopsAuth.AuthIdentity {
    val principal = authentication.principal<TokenValidationContextPrincipal>()
        ?: error("There is no Principal present, make sure you are in an authenticated route")

    val feature = attributes[HopsAuth.key]
    feature.providers.forEach { provider ->
        if (principal.context.hasTokenFor(provider.issuerName)) {
            return provider.extractAuthIdentity(principal.context.getClaims(provider.issuerName))
        }
    }

    error("No tokens found for neither AzureAD nor Maskinporten!")
}
