package e2e

import e2e.tests.LivenessTest
import e2e.tests.Test
import io.ktor.client.HttpClient
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestExecutor(
    val httpClient: HttpClient,
    internalDomain: String,
    private val results: Results = Results(),
) {
    private val log: Logger = LoggerFactory.getLogger(TestExecutor::class.java)
    private val tests = LivenessTest.createAllTests(this, internalDomain)

    inline fun <T> http(http: HttpClient.() -> T) = http(httpClient)
    fun getResults() = results

    suspend fun runTests() = tests.forEach { test ->
        withTimeout(5_000L) { // 5s timeout per test
            when (test.run()) {
                true -> log.info("${test.name} passed")
                false -> results.addFailedTest(test)
            }
        }
    }

    private fun Results.addFailedTest(test: Test) {
        apply {
            test {
                name = test.name
                description = test.description
                stacktrace = test.stacktrace
            }
        }
    }
}
