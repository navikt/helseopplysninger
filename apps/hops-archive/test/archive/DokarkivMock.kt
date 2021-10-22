package archive

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import java.io.Closeable
import kotlinx.coroutines.CompletableDeferred

class DokarkivMock : Closeable {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val receivedRequest = CompletableDeferred<ApplicationCall>()
    private val server = createKtorServer(port).apply { start() }

    private fun createKtorServer(port: Int): NettyApplicationEngine =
        embeddedServer(factory = Netty, port = port) {
            routing {
                post("/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true") {
                    receivedRequest.complete(call)
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

    override fun close() {
        server.stop(100, 100)
    }
}