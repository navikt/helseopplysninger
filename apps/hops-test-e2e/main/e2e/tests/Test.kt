package e2e.tests

interface Test {
    val name: String
    val description: String
    var stacktrace: String?
    suspend fun run(): Boolean
}
