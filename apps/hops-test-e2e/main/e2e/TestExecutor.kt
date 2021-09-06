package e2e

import e2e.tests.AlivenessTest
import e2e.tests.Test
import io.ktor.client.HttpClient
import kotlinx.coroutines.withTimeout

class TestExecutor(
    val httpClient: HttpClient,
    val report: Report = Report()
) {
    inline fun <T> http(http: HttpClient.() -> T) = http(httpClient)

    suspend fun runTests() = tests.forEach {
        withTimeout(10_000L) { // 10s
            runTest(it)
        }
    }

    private suspend fun runTest(test: Test) = when (test.run()) {
        Status.Success -> println("${test.name} passed")
        Status.Failed -> report.addFailedTest(test.name)
    }

    private fun Report.addFailedTest(testName: String) {
        apply {
            test {
                name = testName
            }
        }
    }

    private val tests = listOf<Test>(
        AlivenessTest("hops-api liveness", "hops-api.local.gl:8080", this),
        AlivenessTest("hops-eventreplaykafka liveness", "hops-eventreplaykafka.local.gl:8080", this),
        AlivenessTest("hops-eventsinkkafka liveness", "hops-eventsinkkafka.local.gl:8080", this),
        AlivenessTest("hops-eventstore liveness", "hops-eventstore.local.gl:8080", this),
        AlivenessTest("hops-fileshare liveness", "hops-fileshare.local.gl:8080", this),
        AlivenessTest("hops-test-external liveness", "hops-test-external.local.gl:8080", this),
    )
}
