package fileshare.infrastructure

import io.ktor.auth.Authentication
import no.nav.helse.hops.hoplite.asApplicationConfig
import no.nav.security.token.support.ktor.RequiredClaims
import no.nav.security.token.support.ktor.tokenValidationSupport

object Constants {
    const val EXTERNAL_PROVIDER_UPLOAD = "external-upload"
    const val EXTERNAL_PROVIDER_DOWNLOAD = "external-download"
    const val INTERNAL_PROVIDER = "internal"
}

fun Authentication.Configuration.useNaviktTokenSupport(config: Config.ModuleOAuth) {
    val uploadScope = config.maskinporten.uploadScope
    val downloadScope = config.maskinporten.downloadScope
    val unionClaims = arrayOf("scope=$uploadScope $downloadScope", "scope=$downloadScope $uploadScope")
    val downloadClaims = unionClaims + "scope=$downloadScope"
    val uploadClaims = unionClaims + "scope=$uploadScope"

    val downReqClaims = RequiredClaims(config.maskinporten.issuer.name, downloadClaims, true)
    val upReqClaims = RequiredClaims(config.maskinporten.issuer.name, uploadClaims, true)
    val internalReqClaims = RequiredClaims(config.azure.name, emptyArray(), true)

    val applicationConfig = listOf(config.azure, config.maskinporten.issuer).asApplicationConfig()
    tokenValidationSupport(Constants.EXTERNAL_PROVIDER_UPLOAD, applicationConfig, upReqClaims)
    tokenValidationSupport(Constants.EXTERNAL_PROVIDER_DOWNLOAD, applicationConfig, downReqClaims)
    tokenValidationSupport(Constants.INTERNAL_PROVIDER, applicationConfig, internalReqClaims)
}