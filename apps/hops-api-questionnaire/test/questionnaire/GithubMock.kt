package questionnaire

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

class GithubMock : AutoCloseable {
    private val port = randomPort()
    val url = "http://localhost:$port"
    private val server = createKtorServer(port).apply { start() }

    private fun createKtorServer(port: Int): NettyApplicationEngine =
        embeddedServer(factory = Netty, port = port) {
            routing {

                /**
                 * All releases have browser_download_url's to:
                 * grunnstonad-1.0.0
                 * hjelpestonad-1.0.0
                 */
                get("/repos/navikt/fhir-questionnaires/releases") {
                    val releases = resourceFile("/github/releases.json").replaceHandlebar(port)
                    call.respondText(releases, ContentType.Application.Json)
                }

                /**
                 * Latest release have browser_download_url to:
                 * hjelpestonad-1.0.1
                 */
                get("/repos/navikt/fhir-questionnaires/releases/latest") {
                    val latest = resourceFile("/github/latest.json").replaceHandlebar(port)
                    call.respondText(latest, ContentType.Application.Json)
                }

                get("/download/grunnstonad-1.0.0.json") {
                    val grunnstonad = resourceFile("/questionnaires/grunnstonad-1.0.0.json")
                    call.respondText(grunnstonad, ContentType.Application.Json)
                }

                get("/download/hjelpestonad-1.0.0.json") {
                    val hjelpestonad = resourceFile("/questionnaires/hjelpestonad-1.0.0.json")
                    call.respondText(hjelpestonad, ContentType.Application.Json)
                }

                get("/download/hjelpestonad-1.0.1.json") {
                    val hjelpestonad = resourceFile("/questionnaires/hjelpestonad-1.0.1.json")
                    call.respondText(hjelpestonad, ContentType.Application.Json)
                }
            }
        }

    override fun close() {
        server.stop(100, 100)
    }
}
