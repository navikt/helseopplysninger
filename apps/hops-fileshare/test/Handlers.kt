import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.http.HttpStatusCode
import io.ktor.http.fullPath

object Handlers {
    val happyPathHandler = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                when (request.url.fullPath) {
                    "/storage/v1/b/hops/o/testfile?alt=media" -> respond("downloaded file")
                    "/storage/v1/b/hops/o/nonexistentfile?alt=media" -> respond("bad stuff happened", status = HttpStatusCode.InternalServerError)
                    else -> error("Mock does not respond to: ${request.url}")
                }
            }
        }
    }

    val notFoundHandler = HttpClient(MockEngine) {
        engine {
            addHandler { request ->
                val donwnloadPath = "/storage/v1/b/hops/o/testfile?alt=media"

                when (request.url.fullPath) {
                    donwnloadPath -> respond("downloaded file")
                    else -> error("Mock does not respond to: ${request.url}")
                }
            }
        }
    }
}
