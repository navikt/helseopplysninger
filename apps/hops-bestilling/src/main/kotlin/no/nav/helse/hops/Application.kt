package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallId
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.features.callIdMdc
import io.ktor.features.generate
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import no.nav.helse.hops.Constants.Companion.NAV_CALL_ID
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import org.koin.ktor.ext.Koin

@Suppress("unused") // Referenced in application.conf
fun Application.module() {
    install(DefaultHeaders)
    install(CallId) {
        header(NAV_CALL_ID)
        generate(17)
        verify { it.isNotEmpty() }
    }
    install(CallLogging) {
        callIdMdc(NAV_CALL_ID)
    }
    install(Koin) {
        modules(KoinBootstrapper.singleModule, environment.config.asHoplitePropertySourceModule())
    }

    routing {
        get("/isReady") {
            call.respondText("ready")
        }
        get("/isAlive") {
            call.respondText("alive")
        }
    }
}
