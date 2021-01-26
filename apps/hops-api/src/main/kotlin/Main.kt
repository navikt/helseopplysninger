import io.ktor.application.*
import io.ktor.features.*
import io.ktor.response.*
import io.ktor.routing.*

fun Application.api() {
    install(DefaultHeaders)
    install(CallLogging)
    install(Routing) {
        get("/") {
            call.respondText("api")
        }
        get("/isReady") {
            call.respondText("api")
        }
        get("/isAlive") {
            call.respondText("api")
        }
    }
}