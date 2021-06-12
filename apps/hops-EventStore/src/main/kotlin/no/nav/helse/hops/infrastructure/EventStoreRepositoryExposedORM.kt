package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import no.nav.helse.hops.domain.EventDto
import no.nav.helse.hops.domain.EventStoreReadOnlyRepository
import no.nav.helse.hops.domain.EventStoreRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class EventStoreRepositoryExposedORM : EventStoreRepository {
    init {
        Database.connect("jdbc:h2:mem:test;DB_CLOSE_DELAY=-1", driver = "org.h2.Driver")

        // TODO: Exposed har ikke har støtte for migrations, se derfor på bruk av flywaydb eller andre verktøy for å
        //  generere skjemaer før dette taes i bruk i produksjon.
        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(EventTable, DestinationTable)
        }
    }

    override suspend fun add(event: EventDto) =
        withContext(Dispatchers.IO) {
            transaction {
                val insertedEventId = EventTable.insert {
                    it[bundleId] = event.bundleId
                    it[messageId] = event.messageId
                    it[correlationId] = event.correlationId
                    it[eventType] = event.eventType
                    it[recorded] = event.recorded
                    it[_source] = event.source
                    it[data] = event.data.decodeToString()
                    it[dataType] = event.dataType
                    it[dataBytes] = event.data.size
                }[EventTable.id]

                event.destinations.forEach { dest ->
                    DestinationTable.insert {
                        it[eventId] = insertedEventId
                        it[endpoint] = dest
                    }
                }
            }
        }

    override suspend fun search(query: EventStoreReadOnlyRepository.Query) =
        withContext(Dispatchers.IO) {
            return@withContext transaction {
                return@transaction EventTable.selectAll().map {
                    EventDto(
                        messageId = it[EventTable.messageId],
                        bundleId = it[EventTable.bundleId],
                        correlationId = it[EventTable.correlationId],
                        eventType = it[EventTable.eventType],
                        recorded = it[EventTable.recorded],
                        source = it[EventTable._source],
                        destinations = emptyList(), // Not needed for now.
                        data = it[EventTable.data].toByteArray(),
                        dataType = it[EventTable.dataType]
                    )
                }
            }
        }
}

private object EventTable : Table() {
    val id = long("id").autoIncrement()
    val bundleId = uuid("bundle_id").uniqueIndex()
    val messageId = uuid("message_id").uniqueIndex()
    val correlationId = varchar("correlation_id", 200)
    val eventType = varchar("event_type", 200)
    val recorded = datetime("recorded")
    val _source = varchar("source", 200)
    val data = text("data")
    val dataType = varchar("data_type", 100)
    val dataBytes = integer("data_bytes")

    override val primaryKey = PrimaryKey(id)
}

private object DestinationTable : Table() {
    val id = integer("id").autoIncrement()
    val eventId = long("event_id").references(EventTable.id)
    val endpoint = varchar("endpoint", 200)

    override val primaryKey = PrimaryKey(id)
}
