package infrastructure

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.StringSpec
import io.kotest.inspectors.forAll
import io.kotest.matchers.shouldBe
import io.ktor.auth.Authentication
import io.ktor.config.ApplicationConfigurationException
import io.ktor.config.MapApplicationConfig
import no.nav.security.mock.oauth2.MockOAuth2Server

internal class AuthConfiguratorTest : StringSpec({
    "naviktTokenSupport fail without properties" {
        oAuthProperties.map { (key, _) -> key }.forAll { oAuthProp ->
            val thrown = shouldThrow<ApplicationConfigurationException> {
                Authentication().configure {
                    useNaviktTokenSupport(appConfig(oAuthProp))
                }
            }

            thrown.message shouldBe "Property $oAuthProp not found."
        }
    }

    "naviktTokenSupport succeeds with all properties set with available discoveryurl" {
        Authentication().configure {
            val mockOAuth2Server = MockOAuth2Server()
            mockOAuth2Server.start(8082)
            useNaviktTokenSupport(appConfig())
            mockOAuth2Server.shutdown()
        }
    }
})

val oAuthProperties = listOf(
    "security.scopes.publish" to "amazing-publisher",
    "security.scopes.subscribe" to "fantastic-subscriber",
    "no.nav.security.jwt.issuers.size" to "1",
    "no.nav.security.jwt.issuers.0.issuer_name" to "test-issuer",
    "no.nav.security.jwt.issuers.0.discoveryurl" to "http://localhost:8082/test-issuer/.well-known/openid-configuration",
    "no.nav.security.jwt.issuers.0.accepted_audience" to "test-audience",
)

internal fun appConfig(vararg excludedProps: String) = MapApplicationConfig(
    *oAuthProperties
        .filterNot { (key, _) -> excludedProps.contains(key) }
        .toTypedArray()
)
