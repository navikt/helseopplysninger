package no.nav.helse.hops.routes

import io.ktor.application.*
import io.ktor.response.*
import io.ktor.routing.*
import no.nav.helse.hops.auth.Auth
import java.io.File

fun Routing.indexRoutes() {
    val auth = Auth()
    get("/") {
        call.respondText("Hello Autotest")
    }
    get("/changed") {
        call.respondText("api")
    }
    get("/token") {
        val token = auth.token();
        call.respondText(token.toString())
    }
    get("/bestilling"){
        val fileContent = this.javaClass::class.java.getResource("fixtures/bestilling.json").readText()
        call.respondText(fileContent)
    }
}
