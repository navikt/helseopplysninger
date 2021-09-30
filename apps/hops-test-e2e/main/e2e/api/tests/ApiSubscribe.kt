package e2e.api.tests

import e2e._common.Test
import e2e.api.ApiExternalClient
import io.ktor.http.HttpStatusCode
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readUTF8Line
import org.slf4j.Logger
import org.slf4j.LoggerFactory

internal class ApiSubscribe(override val name: String, private val client: ApiExternalClient) : Test {
    override val description: String = "get fhir resource from event-store"
    override var message: String? = null

    private val log: Logger = LoggerFactory.getLogger("e2e.api.ApiSubscribe")

    override suspend fun test(): Boolean = runSuspendCatching {
        val response = client.get()
        when (response.status) {
            HttpStatusCode.OK -> validateResponse(response.content)
            else -> false
        }
    }

    private suspend fun validateResponse(bytes: ByteReadChannel): Boolean {
        val lines = bytes.readUTF8Line()
        log.info("fhir response: $lines")
        return lines != null
    }
}
