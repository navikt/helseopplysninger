package no.nav.helse.hops.hops.routes

import io.ktor.application.call
import io.ktor.response.respondText
import io.ktor.routing.Routing
import io.ktor.routing.get
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import no.nav.helse.hops.hops.auth.Auth
import no.nav.helse.hops.hops.utils.Fixtures
import no.nav.helse.hops.hops.utils.checkSutServices

fun Routing.indexRoutes() {
    val auth = Auth()
    get("/") {
        call.respondText("Hello Autotest")
    }
    get("/changed") {
        call.respondText("api")
    }
    get("/token") {
        val token = auth.token("myscope")
        call.respondText(token.toString())
    }
    get("/bestilling") {
        val fileContent = Fixtures().bestillingsBundle()
        call.respondText(fileContent.toString())
    }
    get("/apps") {
        val apps = checkSutServices()
        call.respondText(Json.encodeToString(apps))
    }
}
