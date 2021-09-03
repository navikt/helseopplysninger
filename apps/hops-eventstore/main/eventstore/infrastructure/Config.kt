package eventstore.infrastructure

data class Config(val db: Database) {
    data class Database(
        val host: String,
        val port: String,
        val db: String,
        val username: String,
        val password: String,
    ) {
        val url = "jdbc:postgresql://$host:$port/$db"
    }
}
