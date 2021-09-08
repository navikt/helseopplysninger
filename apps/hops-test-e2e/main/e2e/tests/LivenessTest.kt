package e2e.tests

import e2e.Config
import e2e.TestExecutor
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

const val livenessPath = "/isAlive"

internal class LivenessTest(
    private val executor: TestExecutor,
    private val url: String,
) : Test {
    override val name: String = "GET $url$livenessPath"
    override val description: String = "Checks the liveness probe"
    override var stacktrace: String? = null

    override suspend fun run(): Boolean = runCatching {
        executor.http {
            val response = get<HttpResponse>(url + livenessPath)
            when (response.status) {
                HttpStatusCode.OK -> true
                else -> false
            }
        }
    }.getOrElse {
        stacktrace = it.stackTraceToString()
        false
    }

    companion object {
        fun createAllTests(exec: TestExecutor, hopsConfig: Config.Hops) = listOf<Test>(
            LivenessTest(exec, hopsConfig.api.host),
            LivenessTest(exec, hopsConfig.eventreplaykafka.host),
            LivenessTest(exec, hopsConfig.eventsinkkafka.host),
            LivenessTest(exec, hopsConfig.eventstore.host),
            LivenessTest(exec, hopsConfig.fileshare.host),
            LivenessTest(exec, hopsConfig.testExternal.host),
        )
    }
}
