package e2e

import kotlinx.serialization.Serializable

@Serializable
class Results(private val failedTests: MutableList<FailedTest> = mutableListOf()) {
    fun test(init: FailedTest.() -> Unit) = FailedTest().also {
        it.init()
        failedTests.add(it)
    }
}

@Serializable
class FailedTest(
    var name: String = "test",
    var description: String = "description",
    var stacktrace: String? = null,
)
