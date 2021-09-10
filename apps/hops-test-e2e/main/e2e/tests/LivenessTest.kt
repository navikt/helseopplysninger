package e2e.tests

import e2e.Config
import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

const val livenessPath = "/isAlive"

internal class LivenessTest(private val url: String) : Test {
    override val name: String = "GET $url$livenessPath"
    override val description: String = "Checks the liveness probe"
    override var stacktrace: Throwable? = null

    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 1_000L
            connectTimeoutMillis = 1_000L
        }
    }

    override suspend fun run(): Boolean = runCatching {
        when (client.get<HttpResponse>(url + livenessPath).status) {
            HttpStatusCode.OK -> true
            else -> false
        }
    }.getOrElse {
        stacktrace = it // can also throw when httpclient times out
        false
    }

    companion object {
        fun createAllTests(hopsConfig: Config.Hops) = listOf<Test>(
            LivenessTest(hopsConfig.api.host),
            LivenessTest(hopsConfig.eventreplaykafka.host),
            LivenessTest(hopsConfig.eventsinkkafka.host),
            LivenessTest(hopsConfig.eventstore.host),
            LivenessTest(hopsConfig.fileshare.host),
        )
    }
}
