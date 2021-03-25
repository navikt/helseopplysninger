package no.nav.helse.hops

import no.nav.helse.hops.infrastructure.KoinBootstrapper
import org.koin.dsl.koinApplication
import org.koin.logger.SLF4JLogger

fun main() {
    val app = koinApplication {
        SLF4JLogger()
        modules(KoinBootstrapper.module)
    }

    try {
        val service = app.koin.get<Service>()
        service.execute()
    } finally {
        app.close()
    }
}
