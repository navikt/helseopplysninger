package e2e.dsl

import kotlinx.serialization.Serializable

@Serializable
data class Report(val testResults: MutableList<TestResult> = mutableListOf()) {
    fun test(init: TestResult.() -> Unit) = TestResult().also {
        it.init()
        testResults.add(it)
    }
}

@Serializable
enum class Status {
    SUCCESS,
    FAILED
}

@Serializable
data class TestResult(
    var name: String? = null,
    var status: Status? = null,
)
