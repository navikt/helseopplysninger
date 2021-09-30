package e2e.api

import com.nimbusds.jose.jwk.RSAKey
import io.ktor.client.HttpClient
import io.ktor.client.features.auth.Auth
import io.ktor.client.features.auth.AuthProvider
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.accept
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.HttpHeaders
import io.ktor.http.Url
import io.ktor.http.auth.HttpAuthHeader
import io.ktor.http.contentType
import no.nav.helse.hops.convert.ContentTypes.fhirJsonR4
import no.nav.helse.hops.maskinporten.MaskinportClient
import no.nav.helse.hops.maskinporten.MaskinportConfig

private const val subscribePath = "/fhir/4.0/Bundle"
private const val publishPath = "/fhir/4.0/\$process-message"

typealias FhirResource = String

interface ExternalApiFacade {
    suspend fun get(): HttpResponse
    suspend fun post(resource: FhirResource): HttpResponse
}

internal class ApiExternalClient(
    private val httpClient: HttpClient,
    private val config: ApiConfig,
) : ExternalApiFacade {
    override suspend fun get(): HttpResponse =
        httpClient.get("${config.api.hostExternal}$subscribePath?_count=1&_offset=0") {
            accept(fhirJsonR4)
            header("X-Request-ID", "e2e")
        }

    override suspend fun post(resource: FhirResource): HttpResponse =
        httpClient.post("${config.api.hostExternal}$publishPath") {
            contentType(fhirJsonR4)
            header("X-Request-ID", "e2e")
            body = resource
        }
}

internal object HttpClientFactory {
    fun create(config: ApiConfig.Maskinporten) = HttpClient {
        install(Auth) {
            providers.add(MaskinportAuthenticator(config))
        }
    }
}

private class MaskinportAuthenticator(config: ApiConfig.Maskinporten) : AuthProvider {
    override val sendWithoutRequest = true
    override fun isApplicable(auth: HttpAuthHeader) = true
    override suspend fun addRequestHeaders(request: HttpRequestBuilder) {
        request.header(HttpHeaders.Authorization, "Bearer ${maskinporten.jwt.parsedString}")
    }

    private val maskinporten = MaskinportClient(
        MaskinportConfig(
            baseUrl = config.discoveryUrl.withoutPath,
            clientId = config.clientId,
            privateKey = RSAKey.parse(config.clientJwk),
            scope = config.scope,
            resource = config.audience,
            issuer = config.issuer,
        )
    )

    val String.withoutPath: String get() = removeSuffix(Url(this).encodedPath) // http:nice/path/x -> http:nice
}
