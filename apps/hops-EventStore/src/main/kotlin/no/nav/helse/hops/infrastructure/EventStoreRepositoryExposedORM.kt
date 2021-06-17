package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.Dispatchers
import no.nav.helse.hops.domain.EventDto
import no.nav.helse.hops.domain.EventStoreReadOnlyRepository
import no.nav.helse.hops.domain.EventStoreRepository
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.ResultRow
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.andWhere
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.statements.InsertStatement
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction
import java.sql.SQLException
import kotlin.math.max

class EventStoreRepositoryExposedORM(config: Config) : EventStoreRepository {
    data class Config(
        val url: String,
        val username: String = "",
        val password: String = ""
    )

    private val database =
        Database.connect(url = config.url, user = config.username, password = config.password)

    init {
        // TODO: Exposed har ikke har støtte for migrations, se derfor på bruk av flywaydb eller andre verktøy for å
        //  generere skjemaer før dette taes i bruk i produksjon.
        transaction(database) {
            addLogger(Slf4jSqlDebugLogger)
            SchemaUtils.create(EventTable, DestinationTable)
        }
    }

    override suspend fun add(event: EventDto) {
        try {
            newSuspendedTransaction(Dispatchers.IO, database) {
                val rowId = EventTable.insert { it.setValues(event) }[EventTable.id]

                event.destinations.forEach { dest ->
                    DestinationTable.insert {
                        it[eventId] = rowId
                        it[endpoint] = dest
                    }
                }
            }
        } catch (ex: SQLException) {
            // Ignore unique-constraint violations in order to provide idempotent behavior.
            // Exposed will already have logged this as a WARNING.
            val errorCode = max(ex.errorCode, ex.sqlState.toIntOrNull() ?: 0)
            if (errorCode != SqlErrorCodes.uniqueViolation) throw ex
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
                            EventTable.id eq DestinationTable.eventId and(DestinationTable.endpoint eq query.destinationUri)
                        }
                    )
                    .selectAll()

            query.messageId?.let {
                exposedQuery.andWhere { EventTable.messageId eq it }
            }

            exposedQuery
                .orderBy(EventTable.id to SortOrder.ASC)
                .limit(query.count, query.offset)
                .map(::toEventDto)
        }
}

private object EventTable : Table() {
    val id = long("id").autoIncrement()
    val bundleId = uuid("bundle_id").uniqueIndex()
    val messageId = uuid("message_id").uniqueIndex()
    val requestId = varchar("request_id", 200)
    val eventType = varchar("event_type", 200).index()
    val bundleTimestamp = datetime("bundle_timestamp").index()
    val recorded = datetime("recorded")
    val src = varchar("source", 200).index()
    val data = text("data")
    val dataType = varchar("data_type", 100).index()
    val dataBytes = integer("data_bytes")

    override val primaryKey = PrimaryKey(id)
}

private object DestinationTable : Table() {
    val id = integer("id").autoIncrement()
    val eventId = long("event_id").references(EventTable.id)
    val endpoint = varchar("endpoint", 200).index()

    override val primaryKey = PrimaryKey(id)
}

private object SqlErrorCodes {
    const val uniqueViolation = 23505
}

private fun InsertStatement<Number>.setValues(event: EventDto) {
    this[EventTable.bundleId] = event.bundleId
    this[EventTable.messageId] = event.messageId
    this[EventTable.requestId] = event.requestId
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
        requestId = row[EventTable.requestId],
        eventType = row[EventTable.eventType],
        bundleTimestamp = row[EventTable.bundleTimestamp],
        recorded = row[EventTable.recorded],
        source = row[EventTable.src],
        destinations = emptyList(), // Not needed for now.
        data = row[EventTable.data].toByteArray(),
        dataType = row[EventTable.dataType]
    )
