package no.nav.helse.hops.security

import java.net.URL

data class IssuerConfig(
    val name: String,
    val discoveryUrl: URL,
    val audience: String,
    val optionalClaims: String?
)
