package archive.testUtils

import io.ktor.application.ApplicationCall
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine
import kotlinx.coroutines.CompletableDeferred

class Request(val call: ApplicationCall, val body: String)

class DokarkivMock : AutoCloseable {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    val receivedRequest = CompletableDeferred<Request>()
    private val server = createKtorServer(port).apply { start() }

    private fun createKtorServer(port: Int): NettyApplicationEngine =
        embeddedServer(factory = Netty, port = port) {
            routing {
                post("/rest/journalpostapi/v1/journalpost") {
                    receivedRequest.complete(Request(call, call.receiveText()))
                    call.respond(HttpStatusCode.OK)
                }
            }
        }

    override fun close() {
        server.stop(100, 100)
    }
}
