package routes

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get

fun Routing.naisRoutes() {
    get("/isReady") {
        call.respondText("API")
    }
    get("/isAlive") {
        call.respondText("API")
    }
}
