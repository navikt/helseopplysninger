package e2e

import kotlinx.serialization.Serializable

@Serializable
data class Results(
    val failedTests: MutableList<FailedTest> = mutableListOf(),
    val totalDurationInMillis: String = "0ms"
) {
    fun test(init: FailedTest.() -> Unit) = FailedTest().also {
        it.init()
        failedTests.add(it)
    }
}

@Serializable
data class FailedTest(
    var name: String = "test",
    var description: String = "description",
    var durationInMillis: String = "0ms",
    var message: String? = null,
)
