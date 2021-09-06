package e2e.tests

import e2e.TestExecutor
import e2e.Status
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpStatusCode

class AlivenessTest(
    override val name: String,
    private val url: String,
    private val executor: TestExecutor,
) : Test {
    override suspend fun run(): Status = runCatching {
        executor.http {
            when (get<HttpResponse>("http://$url/isAlive").status) {
                HttpStatusCode.OK -> Status.Success
                else -> Status.Failed
            }
        }
    }.getOrElse { Status.Failed }
}
