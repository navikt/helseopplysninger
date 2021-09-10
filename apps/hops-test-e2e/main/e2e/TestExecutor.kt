package e2e

import e2e.tests.LivenessTest
import e2e.tests.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import kotlin.time.Duration
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class TestExecutor(config: Config.Hops) {
    private val log: Logger = LoggerFactory.getLogger(TestExecutor::class.java)
    private val livenessTests = LivenessTest.createAllTests(config)

    suspend fun runTests(): Results {
        val results = Results()
        val duration = measureTime {
            livenessTests.forEach { test ->
                results.run(test)
            }
        }

        return results.copy(totalDurationInMillis = duration.toString(TimeUnit.MILLISECONDS))
    }

    private suspend fun Results.run(test: Test) {
        val (testResult, duration) = measureTimedValue {
            test.run()
        }

        when (testResult) {
            true -> log.info("${test.name} passed in $duration")
            false -> addFailed(test, duration)
        }
    }

    private fun Results.addFailed(test: Test, duration: Duration) {
        log.warn("${test.name} failed in $duration", test.stacktrace)

        apply {
            test {
                name = test.name
                description = test.description
                durationInMillis = duration.toString(TimeUnit.MILLISECONDS)
                message = test.stacktrace?.localizedMessage
            }
        }
    }
}
