package no.nav.helse.hops.security

import com.auth0.jwk.JwkProviderBuilder
import io.ktor.auth.*
import io.ktor.auth.jwt.*
import io.ktor.config.*
import java.util.concurrent.TimeUnit

fun Authentication.Configuration.azureJwt(appConfig: ApplicationConfig) {
    val jwtConfig = appConfig.jwtConfig("jwt")
    val jwkProvider = JwkProviderBuilder(jwtConfig.issuer)
        .cached(10, 24, TimeUnit.HOURS) // cache up to 10 JWKs for 24 hours
        .rateLimited(10, 1, TimeUnit.MINUTES) // if not cached, only allow max 10 different keys per minute to be fetched from external provider
        .build()

    jwt {
        verifier(jwkProvider, jwtConfig.issuer)
        realm = jwtConfig.realm
        validate { credentials ->
            if (credentials.payload.audience.contains(jwtConfig.audience))  JWTPrincipal(credentials.payload)
            else null
        }
    }
}

private fun ApplicationConfig.jwtConfig(scope: String): JwtConfig {
    fun getString(path: String): String = property("$scope.$path").getString()

    return JwtConfig(
        getString("issuer"),
        getString("realm"),
        getString("audience")
    )
}

private data class JwtConfig(val issuer: String, val realm: String, val audience: String)