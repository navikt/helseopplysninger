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
            val rowId = EventTable.insert { it.setValues(event) }[EventTable.id]

            event.destinations.forEach { dest ->
                DestinationTable.insert {
                    it[eventId] = rowId
                    it[endpoint] = dest
                }
            }
        }
    }

    override suspend fun search(query: EventStoreReadOnlyRepository.Query) =
        newSuspendedTransaction(Dispatchers.IO, database) {
            val exposedQuery =
                if (query.destinationUri == null) EventTable.selectAll()
                else EventTable
                    .join(
                        DestinationTable,
                        JoinType.INNER,
                        additionalConstraint = {
                            EventTable.id eq DestinationTable.eventId and (
                                DestinationTable.endpoint eq query.destinationUri
                                )
                        }
                    )
                    .selectAll()

            query.messageId?.let {
                exposedQuery.andWhere { EventTable.messageId eq it }
            }

            if (query.destinationUri == null && query.messageId == null) {
                // Offset using primary-key sequence number, prevents performance issues for large offset values.
                exposedQuery
                    .andWhere { EventTable.id greater query.offset }
                    .orderBy(EventTable.id to SortOrder.ASC)
                    .limit(query.count)
                    .map(::toEventDto)
            } else {
                exposedQuery
                    .orderBy(EventTable.id to SortOrder.ASC)
                    .limit(query.count, query.offset)
                    .map(::toEventDto)
            }
        }
}

private object EventTable : Table() {
    val id = long("id").autoIncrement()
    val bundleId = uuid("bundle_id")
    val messageId = uuid("message_id")
    val eventType = varchar("event_type", 200)
    val bundleTimestamp = datetime("bundle_timestamp")
    val recorded = datetime("recorded")
    val src = varchar("source", 200)
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

private fun InsertStatement<Number>.setValues(event: EventDto) {
    this[EventTable.bundleId] = event.bundleId
    this[EventTable.messageId] = event.messageId
    this[EventTable.eventType] = event.eventType
    this[EventTable.bundleTimestamp] = event.bundleTimestamp
    this[EventTable.recorded] = event.recorded
    this[EventTable.src] = event.source
    this[EventTable.data] = event.data.decodeToString()
    this[EventTable.dataType] = event.dataType
    this[EventTable.dataBytes] = event.data.size
}

private fun toEventDto(row: ResultRow) =
    EventDto(
        messageId = row[EventTable.messageId],
        bundleId = row[EventTable.bundleId],
        eventType = row[EventTable.eventType],
        bundleTimestamp = row[EventTable.bundleTimestamp],
        recorded = row[EventTable.recorded],
        source = row[EventTable.src],
        destinations = emptyList(), // Not needed for now.
        data = row[EventTable.data].toByteArray(),
        dataType = row[EventTable.dataType]
    )
