package no.nav.helse.hops.infrastructure

import kotlinx.coroutines.Dispatchers
import no.nav.helse.hops.domain.EventDto
import no.nav.helse.hops.domain.EventStoreReadOnlyRepository
import no.nav.helse.hops.domain.EventStoreRepository
import org.jetbrains.exposed.exceptions.ExposedSQLException
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.JoinType
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.Slf4jSqlDebugLogger
import org.jetbrains.exposed.sql.SortOrder
import org.jetbrains.exposed.sql.Table
import org.jetbrains.exposed.sql.`java-time`.datetime
import org.jetbrains.exposed.sql.addLogger
import org.jetbrains.exposed.sql.and
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction
import org.jetbrains.exposed.sql.transactions.transaction

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
                val insertedEventId = EventTable.insert {
                    it[bundleId] = event.bundleId
                    it[messageId] = event.messageId
                    it[correlationId] = event.correlationId
                    it[eventType] = event.eventType
                    it[recorded] = event.recorded
                    it[src] = event.source
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
        } catch (ex: ExposedSQLException) {
            // Ignore unique-constraint violations in order to provide idempotent behavior.
            // Exposed will already have logged this as a WARNING.
            if (ex.errorCode != SqlErrorCodes.uniqueViolation) {
                throw ex
            }
        }
    }

    override suspend fun search(filter: EventStoreReadOnlyRepository.Query) =
        newSuspendedTransaction(Dispatchers.IO, database) {
            val query =
                if (filter.destinationUri == null) EventTable.selectAll()
                else EventTable
                    .join(
                        DestinationTable,
                        JoinType.INNER,
                        additionalConstraint = {
                            EventTable.id eq DestinationTable.eventId and(DestinationTable.endpoint eq filter.destinationUri)
                        }
                    )
                    .selectAll()

            query
                .orderBy(EventTable.id to SortOrder.ASC)
                .limit(filter.count, filter.offset)
                .map {
                    EventDto(
                        messageId = it[EventTable.messageId],
                        bundleId = it[EventTable.bundleId],
                        correlationId = it[EventTable.correlationId],
                        eventType = it[EventTable.eventType],
                        recorded = it[EventTable.recorded],
                        source = it[EventTable.src],
                        destinations = emptyList(), // Not needed for now.
                        data = it[EventTable.data].toByteArray(),
                        dataType = it[EventTable.dataType]
                    )
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

private object SqlErrorCodes {
    const val uniqueViolation = 23505
}
