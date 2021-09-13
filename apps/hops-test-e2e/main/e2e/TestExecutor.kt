package e2e

import e2e.tests.LivenessTest
import e2e.tests.Test
import java.util.concurrent.TimeUnit
import kotlin.time.ExperimentalTime
import kotlin.time.measureTime
import kotlin.time.measureTimedValue

@OptIn(ExperimentalTime::class)
class TestExecutor(config: Config.Hops) {

    private val livenessTests = LivenessTest.createAllTests(config)

    suspend fun runTests(): Results {
        val results = Results()
        val duration = measureTime {
            livenessTests.forEach { test -> test.run(results) }
        }
        return results.copy(totalDurationMs = duration.toString(TimeUnit.MILLISECONDS))
    }

    private suspend fun Test.run(results: Results) {
        val (hasPassed, duration) = measureTimedValue { run() }
        when (hasPassed) {
            true -> results.addPassed(this, duration)
            false -> results.addFailed(this, duration)
        }
    }
}
