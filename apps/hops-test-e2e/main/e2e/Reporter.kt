package e2e

import io.ktor.client.HttpClient
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType

suspend fun HttpClient.sendResultsToWorkflow(baseUrl: String, report: Report) =
    post<Report>("$baseUrl/repos/navikt/helseopplysninger/dispatches") {
        header("accept", ContentType("application", "vnd.github.v3+json"))
        body = report
    }
