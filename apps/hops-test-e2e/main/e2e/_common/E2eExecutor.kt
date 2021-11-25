package e2e._common

import kotlinx.serialization.Serializable
import mu.KotlinLogging
import kotlin.time.Duration
import kotlin.time.DurationUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

private val log = KotlinLogging.logger {}

@OptIn(ExperimentalTime::class)
internal class E2eExecutor {
    private val allTests = mutableListOf<Test>()

    suspend fun exec(): Results {
        val results = Results()
        val duration = measureTime {
            allTests.forEach { test -> test.run(results) }
        }
        return results.copy(totalDurationMs = duration.toString(DurationUnit.MILLISECONDS))
    }

    fun add(tests: List<Test>) = allTests.addAll(tests)
    val size: Int get() = allTests.size

    private suspend fun Test.run(results: Results) {
        log.debug { "Running: $name" }
        val (hasPassed, duration) = measureTimedValue { test() }
        when (hasPassed) {
            true -> results.addPassed(this, duration)
            false -> results.addFailed(this, duration)
        }
    }
}

internal fun e2eExecutor(init: E2eExecutor.() -> Unit) = E2eExecutor().apply(init)

@Serializable
@OptIn(ExperimentalTime::class)
data class Results(
    val failedTests: MutableList<FailedTest> = mutableListOf(),
    val totalDurationMs: String = "0ms",
) {
    private fun test(init: FailedTest.() -> Unit) = FailedTest().also {
        it.init()
        failedTests.add(it)
    }

    fun addFailed(test: Test, duration: Duration) = apply {
        log.warn("${test.name} [ FAILED ] in $duration", test.exception)
        test {
            name = test.name
            description = test.description
            durationMs = duration.toString(DurationUnit.MILLISECONDS)
            message = test.exception?.localizedMessage
        }
    }

    fun addPassed(test: Test, duration: Duration) = apply {
        log.info("${test.name} [ PASSED ] in $duration")
    }
}

@Serializable
data class FailedTest(
    var name: String = "test",
    var description: String = "description",
    var durationMs: String = "0ms",
    var message: String? = null,
)
