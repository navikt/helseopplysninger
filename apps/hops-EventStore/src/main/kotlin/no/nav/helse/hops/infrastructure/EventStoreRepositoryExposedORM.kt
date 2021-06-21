package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.Dispatchers
import no.nav.helse.hops.domain.EventDto
import no.nav.helse.hops.domain.EventStoreReadOnlyRepository
import no.nav.helse.hops.domain.EventStoreRepository
import org.flywaydb.core.Flyway
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

class EventStoreRepositoryExposedORM(config: Config) : EventStoreRepository {
    data class Config(
        val url: String,
        val username: String = "",
        val password: String = ""
    )

    private val database =
        Database.connect(url = config.url, user = config.username, password = config.password)

    init {
        Flyway
            .configure()
            .dataSource(config.url, config.username, config.password)
            .load()
            .migrate()
    }

    override suspend fun add(event: EventDto) {
        newSuspendedTransaction(Dispatchers.IO, database) {
            val rowId = EventsTable.insert { it.setValues(event) }[EventsTable.id]

            event.destinations.forEach { dest ->
                DestinationsTable.insert {
                    it[eventId] = rowId
                    it[endpoint] = dest
                }
            }
        }
    }

    override suspend fun search(query: EventStoreReadOnlyRepository.Query) =
        newSuspendedTransaction(Dispatchers.IO, database) {
            val exposedQuery =
                if (query.destinationUri == null) EventsTable.selectAll()
                else EventsTable
                    .join(
                        DestinationsTable,
                        JoinType.INNER,
                        additionalConstraint = {
                            EventsTable.id eq DestinationsTable.eventId and(
                                DestinationsTable.endpoint eq query.destinationUri
                                )
                        }
                    )
                    .selectAll()

            query.messageId?.let {
                exposedQuery.andWhere { EventsTable.messageId eq it }
            }

            exposedQuery
                .orderBy(EventsTable.id to SortOrder.ASC)
                .limit(query.count, query.offset)
                .map(::toEventDto)
        }
}

private object EventsTable : Table() {
    val id = long("id").autoIncrement()
    val bundleId = uuid("bundle_id")
    val messageId = uuid("message_id")
    val eventType = varchar("event_type", 200)
    val bundleTimestamp = datetime("bundle_timestamp")
    val recorded = datetime("recorded")
    val src = varchar("\"source\"", 200)
    val data = text("\"data\"")
    val dataType = varchar("data_type", 100)
    val dataBytes = integer("data_bytes")

    override val primaryKey = PrimaryKey(id)
}

private object DestinationsTable : Table() {
    val id = integer("id").autoIncrement()
    val eventId = long("event_id").references(EventsTable.id)
    val endpoint = varchar("endpoint", 200)

    override val primaryKey = PrimaryKey(id)
}

private fun InsertStatement<Number>.setValues(event: EventDto) {
    this[EventsTable.bundleId] = event.bundleId
    this[EventsTable.messageId] = event.messageId
    this[EventsTable.eventType] = event.eventType
    this[EventsTable.bundleTimestamp] = event.bundleTimestamp
    this[EventsTable.recorded] = event.recorded
    this[EventsTable.src] = event.source
    this[EventsTable.data] = event.data.decodeToString()
    this[EventsTable.dataType] = event.dataType
    this[EventsTable.dataBytes] = event.data.size
}

private fun toEventDto(row: ResultRow) =
    EventDto(
        messageId = row[EventsTable.messageId],
        bundleId = row[EventsTable.bundleId],
        eventType = row[EventsTable.eventType],
        bundleTimestamp = row[EventsTable.bundleTimestamp],
        recorded = row[EventsTable.recorded],
        source = row[EventsTable.src],
        destinations = emptyList(), // Not needed for now.
        data = row[EventsTable.data].toByteArray(),
        dataType = row[EventsTable.dataType]
    )
