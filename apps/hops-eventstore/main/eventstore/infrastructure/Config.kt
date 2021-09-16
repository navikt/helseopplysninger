package eventstore.infrastructure

import no.nav.helse.hops.security.HopsAuth

data class Config(
    val db: Database,
    val oauth: ModuleOAuth,
) {
    data class ModuleOAuth(
        val azure: HopsAuth.Configuration.IssuerConfig,
    )
    data class Database(
        val url: String,
        val username: String,
        val password: String,
    )
}
