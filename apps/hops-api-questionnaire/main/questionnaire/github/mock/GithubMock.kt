package questionnaire.github.mock

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

@Deprecated("Used until tests are implemented")
class GithubMock : AutoCloseable {
    private val server = createKtorServer().apply { start() }

    private fun createKtorServer(): NettyApplicationEngine =
        embeddedServer(factory = Netty, port = 8081) {
            routing {
                get("/questionnaires/releases") {
                    call.respondText(releases, ContentType.Application.Json)
                }

                get("/questionnaires/release/download/hjelpestonad.json") {
                    call.respondText(hjelpestonad)
                }

                get("/questionnaires/release/download/grunnstonad.json") {
                    call.respondText(grunnstonad)
                }
            }
        }

    override fun close() {
        server.stop(100, 100)
    }
}
