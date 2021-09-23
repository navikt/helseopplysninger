package e2e.api.tests

import e2e._common.Test
import e2e.api.ExternalApiFacade
import io.ktor.http.HttpStatusCode

internal class ApiPublish(override val name: String, private val client: ExternalApiFacade) : Test {
    override val description: String = "publish fhir resource to make it available on kafka and eventstore"
    override var stacktrace: Throwable? = null

    override suspend fun run(): Boolean = runCatching {
        val response = client.post()
        when (response.status) {
            HttpStatusCode.Accepted -> isOnKafka and isInEventstore
            else -> false
        }
    }.getOrElse {
        stacktrace = it
        false
    }

    private val isOnKafka: Boolean
        get() = true // todo: implement

    private val isInEventstore: Boolean
        get() = true // todo: implement
}
