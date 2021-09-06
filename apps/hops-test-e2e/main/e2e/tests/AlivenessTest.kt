package e2e.tests

import e2e.TestExecutor
import e2e.dsl.Status
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class AlivenessTest(
    override val name: String,
    private val executor: TestExecutor,
) : Test {
    override suspend fun run(): Status =
        executor.http {
            val response = get<HttpResponse>("http://$name/isAlive")
            when (response.status) {
                HttpStatusCode.OK -> Status.SUCCESS
                else -> Status.FAILED
            }
        }
}
