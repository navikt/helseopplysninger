package no.nav.helse.hops.test.ktor

import io.ktor.application.Application
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationEnvironment
import io.ktor.application.ApplicationFeature
import io.ktor.application.ApplicationStopping
import io.ktor.application.application
import io.ktor.config.MapApplicationConfig
import io.ktor.util.AttributeKey
import no.nav.security.mock.oauth2.MockOAuth2Server

class OAuthMock(configuration: Configuration) {
    private val oauth: MockOAuth2Server = MockOAuth2Server()
    private val shutdownOAuthMock: (Application) -> Unit = { oauth.shutdown() }

    private val size = configuration.issuerSize
    private val name = configuration.issuerName
    private val audience = configuration.acceptedAudience
    private val url = oauth.wellKnownUrl(audience)
    private val additionalProps = configuration.additionalProperties

    class Configuration {
        var issuerSize = 1
        var issuerName = "default"
        var acceptedAudience = "default"
        var additionalProperties = mapOf<String, String>()
    }

    fun getServer() = oauth

    private fun updateConfig(environment: ApplicationEnvironment) {
        environment.monitor.subscribe(ApplicationStopping, shutdownOAuthMock) // stop mock when test is shutting down

        (environment.config as MapApplicationConfig).apply {
            put("no.nav.security.jwt.issuers.size", "$size")
            put("no.nav.security.jwt.issuers.0.issuer_name", name)
            put("no.nav.security.jwt.issuers.0.discoveryurl", "$url")
            put("no.nav.security.jwt.issuers.0.accepted_audience", audience)
            additionalProps.forEach { (key, value) -> put(key, value) }
        }
    }

    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, OAuthMock> {
        override val key = AttributeKey<OAuthMock>("OAuthMock")

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): OAuthMock {
            val config = Configuration().apply(configure)
            val feature = OAuthMock(config)

            pipeline.intercept(ApplicationCallPipeline.Setup) {
                feature.updateConfig(application.environment)
                with(feature.oauth, MockOAuth2Server::start) // start mock before test is run
            }

            return feature
        }
    }
}
