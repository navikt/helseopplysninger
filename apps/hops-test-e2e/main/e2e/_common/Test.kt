package e2e._common

interface Test {
    val name: String
    val description: String
    var stacktrace: Throwable?
    suspend fun run(): Boolean
}
