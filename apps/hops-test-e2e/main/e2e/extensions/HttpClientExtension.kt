package e2e.extensions

import e2e.Config
import e2e.Results
import e2e.TestExecutor
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun HttpClient.runTests(config: Config.Hops): Results =
    TestExecutor(this, config, "dummy", "dummy").let {
        it.runTests()
        it.getResults()
    }

suspend fun HttpClient.sendDispatchEvent(baseUrl: String, results: Results) =
    post<HttpResponse>("$baseUrl/repos/navikt/helseopplysninger/dispatches") {
        contentType(ContentType.Application.Json)
        body = results
    }

internal val GithubJson: ContentType
    get() = ContentType("application", "vnd.github.v3+json")
