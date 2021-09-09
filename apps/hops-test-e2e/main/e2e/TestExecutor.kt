package e2e

import e2e.tests.LivenessTest
import e2e.tests.Test
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TestExecutor(config: Config.Hops) {
    private val log: Logger = LoggerFactory.getLogger(TestExecutor::class.java)
    private val livenessTests = LivenessTest.createAllTests(config)

    suspend fun runTests(): Results {
        val results = Results()

        log.info("Running all tests...")
        livenessTests.forEach { test -> results.verify(test) }
        log.info("Tests completed!")

        return results
    }

    private suspend fun Results.verify(test: Test) {
        when (test.run()) {
            true -> passed(test)
            false -> failed(test)
        }
    }

    private fun passed(test: Test) {
        log.info("${test.name} passed")
    }

    private fun Results.failed(test: Test) {
        log.warn("${test.name} failed", test.stacktrace)

        apply {
            test {
                name = test.name
                description = test.description
                stacktrace = test.stacktrace?.stackTraceToString()
            }
        }
    }
}
