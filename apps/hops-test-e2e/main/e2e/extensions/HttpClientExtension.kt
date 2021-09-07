package e2e.extensions

import e2e.Results
import e2e.TestExecutor
import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType

suspend fun HttpClient.runTests(internalDomain: String): Results =
    TestExecutor(this, internalDomain).let {
        it.runTests()
        it.getResults()
    }

suspend fun HttpClient.sendDispatchEvent(baseUrl: String, results: Results) =
    post<Results>("$baseUrl/repos/navikt/helseopplysninger/dispatches") {
        header(HttpHeaders.Accept, GithubJson)
        contentType(GithubJson)
        body = results
    }

internal val GithubJson: ContentType
    get() = ContentType("application", "vnd.github.v3+json")
