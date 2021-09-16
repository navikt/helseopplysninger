package e2e.api.tests

import e2e._common.Test
import e2e.api.ApiExternalClient
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel

internal class ApiSubscribeTest(private val client: ApiExternalClient) : Test {
    override val name: String = "subsribe external"
    override val description: String = "get fhir resource from `event-store` via `api` through maskinporten"
    override var stacktrace: Throwable? = null

    override suspend fun run(): Boolean = runCatching {
        val response = client.get()
        when (response.status) {
            HttpStatusCode.OK -> validateResponse(response.content)
            else -> false
        }
    }.getOrElse {
        stacktrace = it
        false
    }

    private fun validateResponse(bytes: ByteReadChannel): Boolean {
        return true
    }
}
