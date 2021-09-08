package e2e

import e2e.tests.LivenessTest
import e2e.tests.Test
import io.ktor.client.HttpClient
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestExecutor(
    val httpClient: HttpClient,
    config: Config.Hops,
    workflowId: String,
    appName: String,
) {
    private val log: Logger = LoggerFactory.getLogger(TestExecutor::class.java)
    private val tests = LivenessTest.createAllTests(this, config)
    private val results: Results = Results("e2e", ClientPayload(workflowId, appName))

    inline fun <T> http(http: HttpClient.() -> T) = http(httpClient)
    fun getResults() = results

    suspend fun runTests() = tests.forEach { test ->
        withTimeout(1_000L) { // 1s timeout per test
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
