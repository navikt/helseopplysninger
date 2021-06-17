package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.rest.client.api.IGenericClient
import io.mockk.mockk
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.MockConsumer
import org.apache.kafka.clients.consumer.OffsetResetStrategy
import org.apache.kafka.clients.producer.MockProducer
import org.apache.kafka.clients.producer.Producer
import org.hl7.fhir.instance.model.api.IBaseResource
import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.koin.dsl.module
import org.koin.test.check.checkModules

class KoinBootstrapperTest {

    @Test
    @Disabled
    fun `check all Koin modules`() {
        val testKoinModule = module(override = true) {
            single<Producer<Unit, IBaseResource>> { MockProducer() }
            single<Consumer<Unit, IBaseResource>> { MockConsumer(OffsetResetStrategy.EARLIEST) }
            single { mockk<IGenericClient>() }
        }

        checkModules {
            modules(KoinBootstrapper.singleModule, testKoinModule)
        }
    }
}
