package no.nav.helse.hops.domain

import kotlinx.coroutines.flow.Flow
import org.hl7.fhir.r4.model.Task
import java.time.LocalDateTime

data class TaskChange(val current: Task, val previous: Task?)

interface TaskChangeFeed {
    fun poll(since: LocalDateTime? = null): Flow<TaskChange>
}