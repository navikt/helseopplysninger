package no.nav.helse.hops.infrastructure

import no.nav.helse.hops.domain.EventDto
import no.nav.helse.hops.domain.EventStore
import no.nav.helse.hops.domain.EventStoreReadOnly
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.StdOutSqlLogger
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction

class EventStoreExposedORM : EventStore {
    init {
        Database.connect("jdbc:h2:mem:test", driver = "org.h2.Driver", user = "root", password = "")

        transaction {
            addLogger(StdOutSqlLogger)
            SchemaUtils.create(EventTable, DestinationTable)
        }
    }

    override suspend fun add(event: EventDto) {
        transaction {
            val insertedEventId = EventTable.insert {
                it[bundleId] = event.bundleId
                it[messageId] = event.messageId
                it[correlationId] = event.correlationId
                it[eventType] = event.eventType
                it[recorded] = event.recorded
                it[timestamp] = event.timestamp
                it[_source] = event.source
                it[data] = event.data
                it[dataType] = event.dataType
            }[EventTable.id]

            event.destinations.forEach { dest ->
                DestinationTable.insert {
                    it[eventId] = insertedEventId
                    it[endpoint] = dest
                }
            }
        }
    }

    override suspend fun search(query: EventStoreReadOnly.Query): List<EventDto> {
        var hits = emptyList<EventDto>()

        transaction {
            hits = EventTable.selectAll().map {
                EventDto(
                    messageId = it[EventTable.messageId],
                    bundleId = it[EventTable.bundleId],
                    correlationId = it[EventTable.correlationId],
                    eventType = it[EventTable.eventType],
                    recorded = it[EventTable.recorded],
                    timestamp = it[EventTable.timestamp],
                    source = it[EventTable._source],
                    destinations = emptyList(), // Not needed for now.
                    data = it[EventTable.data],
                    dataType = it[EventTable.dataType],
                )
            }
        }

        return hits
    }
}

private object EventTable : Table() {
    val id = long("id").autoIncrement()
    val bundleId = uuid("bundle_id").uniqueIndex()
    val messageId = uuid("message_id").uniqueIndex()
    val correlationId = varchar("correlation_id", 200)
    val eventType = varchar("event_type", 200)
    val recorded = datetime("recorded")
    val timestamp = datetime("timestamp")
    val _source = varchar("source", 200)
    val data = binary("data")
    val dataType = varchar("data_type", 100)

    override val primaryKey = PrimaryKey(id)
}

private object DestinationTable : Table() {
    val id = integer("id").autoIncrement()
    val eventId = long("event_id").references(EventTable.id)
    val endpoint = varchar("endpoint", 200)

    override val primaryKey = PrimaryKey(id)
}
