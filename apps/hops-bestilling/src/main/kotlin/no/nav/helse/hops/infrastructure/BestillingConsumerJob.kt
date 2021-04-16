package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.parser.DataFormatException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import no.nav.helse.hops.domain.FhirMessageProcessor
import org.apache.kafka.clients.consumer.Consumer
import org.apache.kafka.clients.consumer.ConsumerRecords
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.Bundle
import org.slf4j.Logger
import java.io.Closeable
import java.time.Duration
import kotlin.coroutines.CoroutineContext

class BestillingConsumerJob(
    private val consumer: Consumer<Unit, IBaseResource>,
    private val messageProcessor: FhirMessageProcessor,
    private val logger: Logger,
    config: Configuration.Kafka,
    context: CoroutineContext = Dispatchers.Default
) : Closeable {
    private val scope = CoroutineScope(context)

    init {
        scope.launch {
            consumer.subscribe(listOf(config.topic))

            while (isActive) {
                try {
                    val records = consumer.poll(Duration.ofSeconds(1))

                    records.forEach {
                        val bundle = it.value() as Bundle
                        messageProcessor.process(bundle)
                    }
                }
                catch (ex: DataFormatException) {
                    logger.error("Unable to parse received message on topic={}, error={}", config.topic, ex.message)
                }
            }
        }
    }

    override fun close() {
        scope.cancel()
    }
}