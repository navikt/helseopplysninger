package no.nav.helse.hops

import no.nav.helse.hops.koinBootstrapping.Bootstrapper
import org.koin.dsl.koinApplication
import org.koin.logger.SLF4JLogger

fun main() {
    val app = koinApplication {
        fileProperties("/application.properties")
        SLF4JLogger()
        modules(Bootstrapper.koinModule)
    }

    try {
        val service = app.koin.get<Service>()
        service.execute()
    } finally {
        app.close()
    }
}
