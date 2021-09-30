package e2e.kafka

import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.apache.kafka.clients.consumer.ConsumerRebalanceListener
import org.apache.kafka.clients.consumer.ConsumerRecord
import org.apache.kafka.clients.consumer.KafkaConsumer
import org.apache.kafka.common.TopicPartition
import org.slf4j.LoggerFactory
import java.io.Closeable
import java.lang.invoke.MethodHandles
import java.time.Duration
import java.time.ZonedDateTime
import java.util.UUID

internal class FhirKafkaListener(private val consumer: KafkaConsumer<UUID, ByteArray>) {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())

    /**
     * Called before tests starts to run. Listens for kafka messages from
     * subscribed topics in a background thread and stores them in a list
     */
    fun subscribe(vararg topics: String): KafkaSubscription {
        require(topics.isNotEmpty())

        subscribeAndWait(topics.toMutableSet())
        val subscription = KafkaSubscription(consumer)
        log.info("FhirKafkaListener started...")
        return subscription
    }

    /**
     * Wait synchronously for partitions to be assigned for all consumed topics before the tests starts
     * to avoid missing any messages in our test verification
     */
    private fun subscribeAndWait(unassignedTopics: MutableSet<String>) {
        class TopicAssignementListener<K, V>(private val consumer: KafkaConsumer<K, V>) : ConsumerRebalanceListener {
            override fun onPartitionsAssigned(partitions: MutableCollection<TopicPartition>) {
                consumer.seekToEnd(partitions.filter { unassignedTopics.contains(it.topic()) })
                unassignedTopics.removeAll(partitions.map { it.topic() })
            }

            override fun onPartitionsRevoked(partitions: MutableCollection<TopicPartition>) {
                if (partitions.isNotEmpty()) log.warn("Partitions revoked: $partitions")
            }
        }

        consumer.subscribe(unassignedTopics, TopicAssignementListener(consumer))

        //  We need to poll before partitions are assigned.
        var counterStart = ZonedDateTime.now()
        while (unassignedTopics.isNotEmpty()) {
            consumer.poll(0L.duration)
            if (ZonedDateTime.now().isAfter(counterStart.plusSeconds(3))) {
                log.info("Waiting for assignment for topics: $unassignedTopics")
                counterStart = ZonedDateTime.now()
            }
        }

        log.info("All topics assigned...")
    }
}

private const val sec1 = 1_000L
private const val sec20 = 20_000L
private const val min1 = 60_000L
private val Long.duration: Duration get() = Duration.ofMillis(this)

class KafkaSubscription(private val consumer: KafkaConsumer<UUID, ByteArray>) : Closeable {
    private val log = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass())
    private val consumedRecords = mutableListOf<ConsumerRecord<UUID, ByteArray>>()
    private val job = CoroutineScope(Dispatchers.IO).launch {
        while (isActive) {
            runCatching {
                while (true) {
                    consumer.poll(sec20.duration).forEach { record ->
                        consumedRecords.add(record)
                        log.info("Consumed message: $record")
                    }

                    deleteOneMinOldrecords()
                    isHealthy = true
                }
            }.onFailure { exception ->
                isHealthy = false
                log.error("Error reading topic in test", exception)
                if (exception is CancellationException) throw exception
                delay(sec1)
            }
        }
    }

    @Volatile
    var isHealthy = true
        private set

    override fun close() = runBlocking {
        job.cancel("test complete")
    }

    fun getMessages(topic: String): List<ConsumerRecord<UUID, ByteArray>> =
        ArrayList(consumedRecords).filter { record ->
            record.topic() == topic
        }

    private fun deleteOneMinOldrecords() =
        consumedRecords.removeIf { record ->
            record.timestamp() < System.currentTimeMillis() - min1
        }
}
