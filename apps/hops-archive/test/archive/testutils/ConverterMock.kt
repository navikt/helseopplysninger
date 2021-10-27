package archive.testutils

import io.ktor.application.call
import io.ktor.http.ContentType
import io.ktor.response.respondBytes
import io.ktor.routing.post
import io.ktor.routing.routing
import io.ktor.server.engine.embeddedServer
import io.ktor.server.netty.Netty
import io.ktor.server.netty.NettyApplicationEngine

class ConverterMock : AutoCloseable {
    private val port = getRandomPort()
    val url = "http://localhost:$port"
    private val server = createKtorServer(port).apply { start() }

    private fun createKtorServer(port: Int): NettyApplicationEngine =
        embeddedServer(factory = Netty, port = port) {
            routing {
                post("/\$convert") {
                    val pdf = readResourcesFile("/example.pdf")
                    call.respondBytes(pdf, ContentType.Application.Pdf)
                }
            }
        }

    override fun close() {
        server.stop(100, 100)
    }
}
