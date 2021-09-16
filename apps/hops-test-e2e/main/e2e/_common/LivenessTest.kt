package e2e._common

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

private const val livenessPath = "/isAlive"

internal class LivenessTest(
    override val name: String,
    private val url: String,
) : Test {
    override val description: String = "Checks the liveness probe"
    override var stacktrace: Throwable? = null

    override suspend fun run(): Boolean = runCatching {
        val response = client.get<HttpResponse>(url + livenessPath)
        when (response.status) {
            HttpStatusCode.OK -> true
            else -> false
        }
    }.getOrElse {
        stacktrace = it
        false
    }

    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 1_000L
            connectTimeoutMillis = 1_000L
        }
    }
}
