package e2e._common

import io.ktor.client.HttpClient
import io.ktor.client.features.HttpTimeout
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

private const val livenessPath = "/isAlive"

internal class Liveness(
    override val name: String,
    private val url: String,
    override val description: String = "Checks the liveness probe",
    override var exception: Throwable? = null,
) : Test {

    override suspend fun test(): Boolean = runSuspendCatching {
        val response = client.get<HttpResponse>(url + livenessPath)

        when (response.status) {
            HttpStatusCode.OK -> true
            else -> false
        }
    }

    private val client: HttpClient = HttpClient {
        install(HttpTimeout) {
            requestTimeoutMillis = 5_000L
            connectTimeoutMillis = 5_000L
        }
    }
}
