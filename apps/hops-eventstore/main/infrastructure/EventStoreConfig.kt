package infrastructure

data class EventStoreConfig(val db: Database) {
    data class Database(
        val url: String,
        val username: String,
        val password: String,
    )
}
