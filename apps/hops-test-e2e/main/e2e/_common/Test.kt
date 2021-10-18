package e2e._common

interface Test {
    val name: String
    val description: String
    var exception: Throwable?

    suspend fun test(): Boolean

    suspend fun runSuspendCatching(test: suspend () -> Boolean): Boolean =
        runCatching {
            test()
        }.getOrElse {
            exception = it
            false
        }
}
