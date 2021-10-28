package questionnaire.github

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

class GithubMock : AutoCloseable {
    private val server = createKtorServer().apply { start() }

    private fun createKtorServer(): NettyApplicationEngine =
        embeddedServer(factory = Netty, port = 8081) {
            routing {
                get("/questionnaires/releases") {
                    call.respondText(releases)
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
