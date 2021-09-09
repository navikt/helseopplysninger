package e2e

import e2e.tests.LivenessTest
import e2e.tests.Test
import io.ktor.client.HttpClient
import kotlinx.coroutines.withTimeout
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestExecutor(
    val client: HttpClient,
    config: Config.Hops,
    val workflowId: String,
    val appName: String,
    val testScope: TestScope,
) {
    private val log: Logger = LoggerFactory.getLogger(TestExecutor::class.java)
    private val tests = LivenessTest.createAllTests(this, config)
    private val results: Results = Results("e2e", ClientPayload(workflowId, appName))

    inline fun <T> http(http: HttpClient.() -> T) = http(client)

    suspend fun runTests(): Results {
        log.info("Running $testScope tests...")
        tests.forEach { test ->
            withTimeout(1_000L) { // 1s timeout per test
                when (test.run()) {
                    true -> log.info("${test.name} passed")
                    false -> results.addFailedTest(test)
                }
            }
        }
        log.info("Tests completed!")
        return results
    }

    private fun Results.addFailedTest(test: Test) {
        log.info("${test.name} failed")
        apply {
            test {
                name = test.name
                description = test.description
                stacktrace = test.stacktrace
            }
        }
    }
}
