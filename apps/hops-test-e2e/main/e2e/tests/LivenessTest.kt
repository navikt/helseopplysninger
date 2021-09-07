package e2e.tests

import e2e.TestExecutor
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

internal class LivenessTest(
    override val name: String,
    private val url: String,
    private val domain: String,
    private val executor: TestExecutor,
) : Test {

    override val description: String = "Checks the liveness probe of a service"
    override var stacktrace: String? = null

    override suspend fun run(): Boolean = runCatching {
        executor.http {
            val response = get<HttpResponse>("$url$domain/isAlive")
            when (response.status) {
                HttpStatusCode.OK -> true
                else -> false
            }
        }
    }.getOrElse {
        stacktrace = it.message
        false
    }

    companion object {
        fun createAllTests(exec: TestExecutor, internalDomain: String) = listOf<Test>(
            LivenessTest(
                name = "GET http://hops-api/isAlive",
                url = "http://hops-api",
                domain = internalDomain,
                executor = exec
            ),
            LivenessTest(
                name = "GET http://hops-eventreplaykafka/isAlive",
                url = "https://hops-eventreplaykafka",
                domain = internalDomain,
                executor = exec
            ),
            LivenessTest(
                name = "GET http://hops-eventsinkkafka/isAlive",
                url = "https://hops-eventsinkkafka",
                domain = internalDomain,
                executor = exec
            ),
            LivenessTest(
                name = "GET http://hops-eventstore/isAlive",
                url = "https://hops-eventstore",
                domain = internalDomain,
                executor = exec
            ),
            LivenessTest(
                name = "GET http://hops-fileshare/isAlive",
                url = "https://hops-fileshare",
                domain = internalDomain,
                executor = exec
            ),
            LivenessTest(
                name = "GET http://hops-test-external/isAlive",
                url = "https://hops-test-external",
                domain = internalDomain,
                executor = exec
            ),
        )
    }
}
