package eventstore.infrastructure

data class Config(val db: Database) {
    sealed class Database {
        data class H2(
            val host: String,
            val port: String,
            val db: String,
            val username: String,
            val password: String,
            val url: String,
        ) : Database()

        data class Postgres(
            val host: String,
            val port: String,
            val db: String,
            val username: String,
            val password: String,
        ) : Database() {
            val url: String = "jdbc:postgresql://$host:$port/$db"
        }
    }
}
