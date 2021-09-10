package e2e

import e2e.tests.Test
import kotlinx.serialization.Serializable
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime

@Serializable
@OptIn(ExperimentalTime::class)
data class Results(
    val failedTests: MutableList<FailedTest> = mutableListOf(),
    val totalDurationMs: String = "0ms",
) {
    private val log: Logger = LoggerFactory.getLogger(Results::class.java)
    private fun test(init: FailedTest.() -> Unit) = FailedTest().also {
        it.init()
        failedTests.add(it)
    }

    fun addFailed(test: Test, duration: Duration) = apply {
        log.warn("${test.name} failed in $duration", test.stacktrace)
        test {
            name = test.name
            description = test.description
            durationMs = duration.toString(TimeUnit.MILLISECONDS)
            message = test.stacktrace?.localizedMessage
        }
    }

    fun addPassed(test: Test, duration: Duration) = apply {
        log.info("${test.name} passed in $duration")
    }
}

@Serializable
data class FailedTest(
    var name: String = "test",
    var description: String = "description",
    var durationMs: String = "0ms",
    var message: String? = null,
)
