package no.nav.helse.hops

import no.nav.helse.hops.koinBootstrapping.Bootstrapper
import no.nav.helse.hops.koinBootstrapping.Configuration
import org.koin.dsl.koinApplication
import org.koin.logger.SLF4JLogger

fun main() {
    val app = koinApplication {
        SLF4JLogger()
        modules(Bootstrapper.koinModule, Configuration.koinModule)
    }

    try {
        val service = app.koin.get<Service>()
        service.execute()
    } finally {
        app.close()
    }
}
