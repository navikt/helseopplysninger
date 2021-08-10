package infrastructure

import domain.EventStore
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType

class EventStoreHttp(
    private val httpClient: HttpClient,
    private val config: Configuration.EventStore
) : EventStore {
    override suspend fun search() =
        httpClient.get<HttpResponse>("${config.baseUrl}/fhir/4.0/Bundle?_count=1") {
            accept(ContentType("application", "fhir+json").withParameter("fhirVersion", "4.0"))
        }
}
