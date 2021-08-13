package routes

import domain.StorageClient
import io.ktor.application.call
import io.ktor.auth.authenticate
import io.ktor.http.HttpStatusCode
import io.ktor.request.contentType
import io.ktor.response.respond
import io.ktor.routing.Routing
import io.ktor.routing.get
import io.ktor.routing.post
import io.ktor.routing.route

fun Routing.storageRoutes(storageClient: StorageClient) {
    authenticate {
        route("files") {
            post("/") {
                val fileName = storageClient.save(call.request.receiveChannel(), call.request.contentType())

                call.respond(HttpStatusCode.Created, fileName)
            }

            get("/{fileName}") {
                call.parameters["fileName"]
            }
        }
    }
}
