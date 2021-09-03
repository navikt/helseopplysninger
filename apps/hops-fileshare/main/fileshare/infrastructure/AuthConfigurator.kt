package fileshare.infrastructure

import io.ktor.auth.Authentication
import io.ktor.config.ApplicationConfig
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.tokenValidationSupport

object Constants {
    const val EXTERNAL_PROVIDER_UPLOAD = "external-upload"
    const val EXTERNAL_PROVIDER_DOWNLOAD = "external-download"
    const val INTERNAL_PROVIDER = "internal"
}

fun Authentication.Configuration.useNaviktTokenSupport(config: ApplicationConfig) {
    val uploadScope = config.property("security.scopes.upload").getString()
    val downloadScope = config.property("security.scopes.download").getString()

    val issuerRequiringClaims = config.configList("no.nav.security.jwt.issuers")
        .find { it.propertyOrNull("requires_scope_claims")?.getString() == "true" }
        ?.property("issuer_name")
        ?.getString() ?: throw RuntimeException("Could not find issuer that requires scope claims")

    val issuerNotRequiringClaims = config.configList("no.nav.security.jwt.issuers")
        .filterNot { it.propertyOrNull("requires_scope_claims")?.getString() == "true" }
        .firstOrNull()
        ?.property("issuer_name")
        ?.getString() ?: throw RuntimeException("Could not find issuer that does not require scope claims")

    val unionClaims = arrayOf("scope=$uploadScope $downloadScope", "scope=$downloadScope $uploadScope")
    val downloadClaims = unionClaims + "scope=$downloadScope"
    val uploadClaims = unionClaims + "scope=$uploadScope"

    val downReqClaims = RequiredClaims(issuerRequiringClaims, downloadClaims, true)
    val upReqClaims = RequiredClaims(issuerRequiringClaims, uploadClaims, true)
    val internalReqClaims = RequiredClaims(issuerNotRequiringClaims, emptyArray(), true)

    tokenValidationSupport(Constants.EXTERNAL_PROVIDER_UPLOAD, config, upReqClaims)
    tokenValidationSupport(Constants.EXTERNAL_PROVIDER_DOWNLOAD, config, downReqClaims)
    tokenValidationSupport(Constants.INTERNAL_PROVIDER, config, internalReqClaims)
}