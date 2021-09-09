package e2e.extensions

import e2e.Results
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.contentType

suspend fun HttpClient.sendDispatchEvent(baseUrl: String, body: Results) =
    post<HttpResponse>("$baseUrl/repos/navikt/helseopplysninger/dispatches") {
        contentType(ContentType.Application.Json)
        this.body = body
    }

internal val GithubJson: ContentType
    get() = ContentType("application", "vnd.github.v3+json")
