package eventstore.infrastructure

data class Config(val db: Database) {
    data class Database(
        val url: String,
        val username: String,
        val password: String,
    )
}
