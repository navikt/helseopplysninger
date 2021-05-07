package no.nav.helse.hops

import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.features.CallLogging
import io.ktor.features.DefaultHeaders
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import no.nav.helse.hops.hoplite.asHoplitePropertySourceModule
import no.nav.helse.hops.infrastructure.KoinBootstrapper
import org.koin.core.module.Module
import org.koin.ktor.ext.Koin
import org.koin.ktor.ext.modules

@Suppress("unused") // Referenced in application.conf
fun Application.main(vararg koinModules: Module) {
    install(DefaultHeaders)
    install(CallLogging)
    install(Koin) {
        modules(KoinBootstrapper.singleModule, environment.config.asHoplitePropertySourceModule())
        modules(koinModules.toList())
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
