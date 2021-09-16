package e2e.api.tests

import e2e._common.Test
import e2e.api.ApiExternalClient
import io.ktor.http.HttpStatusCode

internal class ApiPublishTest(private val client: ApiExternalClient) : Test {
    override val name: String = "publish external"
    override val description: String = "external published fhir resource available on kafka and eventstore"
    override var stacktrace: Throwable? = null

    override suspend fun run(): Boolean = runCatching {
        val response = client.post()
        when (response.status) {
            HttpStatusCode.OK -> isOnKafka and isInEventstore
            else -> false
        }
    }.getOrElse {
        stacktrace = it
        false
    }

    private val isOnKafka by lazy {
        true // todo: implement
    }

    private val isInEventstore by lazy {
        true // todo: implement
    }
}
