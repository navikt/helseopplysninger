package eventstore.infrastructure

import no.nav.helse.hops.hoplite.OauthIssuerConfig

data class Config(val db: Database, val oauthIssuers: List<OauthIssuerConfig>) {
    data class Database(
        val url: String,
        val username: String,
        val password: String,
    )
}
