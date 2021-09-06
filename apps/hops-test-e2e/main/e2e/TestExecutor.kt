package e2e

import e2e.dsl.Report
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
        withTimeout(10_000L) {
            runTest(it)
        }
    }

    private suspend fun runTest(test: Test) {
        val result = test.run()
        report.apply {
            test {
                name = test.name
                status = result
            }
        }
    }

    private val tests = listOf<Test>(
        AlivenessTest("localhost:8085", this),
//        AlivenessTest("hops-api", this),
//        AlivenessTest("hops-eventreplaykafka", this),
//        AlivenessTest("hops-eventsinkkafka", this),
//        AlivenessTest("hops-eventstore", this),
//        AlivenessTest("hops-fileshare", this),
//        AlivenessTest("hops-test-external", this),
    )
}
