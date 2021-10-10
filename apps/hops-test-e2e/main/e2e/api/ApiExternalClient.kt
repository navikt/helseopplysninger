package e2e.api

import e2e.fhir.FhirContent
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.contentType
import no.nav.helse.hops.convert.ContentTypes.fhirJsonR4

private const val subscribePath = "/fhir/4.0/Bundle"
private const val publishPath = "/fhir/4.0/\$process-message"

interface ExternalApiFacade {
    suspend fun get(): HttpResponse
    suspend fun post(resource: FhirContent): HttpResponse
}

internal class ApiExternalClient(
    private val config: ApiConfig,
    private val apiGetClient: HttpClient,
    private val apiPostClient: HttpClient,
) : ExternalApiFacade {
    override suspend fun get(): HttpResponse =
        apiGetClient.get("${config.api.hostExternal}$subscribePath?_count=1&_offset=0") {
            accept(fhirJsonR4)
            header("X-Request-ID", "e2e")
        }

    override suspend fun post(resource: FhirContent): HttpResponse =
        apiPostClient.post("${config.api.hostExternal}$publishPath") {
            contentType(fhirJsonR4)
            header("X-Request-ID", "e2e")
            body = resource
        }
}
