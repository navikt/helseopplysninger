package test.external.infrastructure

import test.external.domain.ExternalApiFacade
import io.ktor.client.HttpClient
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType

class ExternalApiHttp(
    private val httpClient: HttpClient,
    private val config: TestExternalConfig.ExternalApi
) : ExternalApiFacade {
    override suspend fun get() =
        httpClient.get<HttpResponse>("${config.baseUrl}/fhir/4.0/Bundle?_count=1") {
            accept(ContentType("application", "fhir+json").withParameter("fhirVersion", "4.0"))
        }
}
