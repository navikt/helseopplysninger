import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.MockRequestHandleScope
import io.ktor.client.engine.mock.respond
import io.ktor.client.request.HttpResponseData
import io.ktor.http.HttpHeaders
import io.ktor.http.fullPath
import io.ktor.http.headersOf
import io.ktor.http.withCharset
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication
import no.nav.helse.hops.convert.ContentTypes
import no.nav.helse.hops.test.startOAuthMock
import no.nav.helse.hops.test.stopOAuthMock
import no.nav.helse.hops.test.testConfig
import org.koin.core.module.Module

@Suppress("unused") // Referenced by kotest
internal object KotestProjectConfig : AbstractProjectConfig() {
    override fun listeners(): List<Listener> = super.listeners() + KotestProjectListener
}

internal object KotestProjectListener : ProjectListener {
    override suspend fun beforeProject() = startOAuthMock()
    override suspend fun afterProject() = stopOAuthMock()
}

internal fun createEventStoreMockClient() =
    HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.url.fullPath) {
                    "/fhir/4.0/Bundle" -> getRequestStub()
                    "/fhir/4.0/\$process-message" -> postResponseStub()
                    else -> error("Unhandled ${request.url}")
                }
            }
        }
    }

private fun MockRequestHandleScope.getRequestStub(): HttpResponseData = respond(
    headers = headersOf(HttpHeaders.ContentType, ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()),
    content = """{"resourceType": "Bundle"}""",
)

private fun MockRequestHandleScope.postResponseStub(): HttpResponseData = respond(
    headers = headersOf(HttpHeaders.ContentType, ContentTypes.fhirJsonR4.withCharset(Charsets.UTF_8).toString()),
    content = "",
)

internal fun <R> withHopsTestApplication(
    koinModule: Module = Module(),
    testContext: TestApplicationEngine.() -> R
): R = withTestApplication(
    {
        testConfig()
        module(koinModule)
    },
    testContext
)
