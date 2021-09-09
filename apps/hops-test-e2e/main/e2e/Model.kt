package e2e

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class Results(
    @SerialName("event_type") // required by GitHub
    val eventType: String,
    @SerialName("client_payload") // requried by GitHub
    val clientPayload: ClientPayload,
) {
    fun test(init: FailedTest.() -> Unit) = FailedTest().also {
        it.init()
        clientPayload.add(it)
    }
}

@Serializable
data class FailedTest(
    var name: String = "test",
    var description: String = "description",
    var stacktrace: String? = null,
)

@Serializable
data class ClientPayload(
    val workflowId: String,
    val appId: String,
    val failedTests: MutableList<FailedTest> = mutableListOf(),
) {
    fun add(failedTest: FailedTest) {
        failedTests.add(failedTest)
    }
}

@Serializable
data class TestRequest(
    val appName: String,
    val workflowId: String,
    val testScope: TestScope = TestScope.ALL,
)

@Serializable
enum class TestScope {
    ALL
}
