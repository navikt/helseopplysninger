package e2e

import kotlinx.serialization.Serializable

@Serializable
data class Report(val testResults: MutableList<TestResult> = mutableListOf()) {
    fun test(init: TestResult.() -> Unit) = TestResult().also {
        it.init()
        testResults.add(it)
    }
}

@Serializable
sealed class Status {
    object Success : Status()
    object Failed : Status()
}

@Serializable
data class TestResult(
    var name: String? = null,
    val status: Status = Status.Success,
)
