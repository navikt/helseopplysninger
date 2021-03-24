package no.nav.helse.hops.koinBootstrapping

import com.sksamuel.hoplite.ConfigLoader
import org.koin.dsl.module

object Configuration {
    val koinModule = module {
        data class Root(val kafka: Kafka)
        single { ConfigLoader().loadConfigOrThrow<Root>("/application.properties") }
        single { get<Root>().kafka }
    }

    data class Kafka(val host: String, val groupId: String, val topic: String)
}
