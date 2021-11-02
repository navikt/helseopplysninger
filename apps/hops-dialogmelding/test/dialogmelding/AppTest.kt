package dialogmelding

import dialogmelding.testutils.Mocks
import dialogmelding.testutils.withTestApp
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import org.junit.jupiter.api.Test
import kotlin.test.assertEquals

class AppTest {
    @Test
    fun `isAlive returns 200 OK`() {
        Mocks().use {
            withTestApp(it) {
                with(handleRequest(HttpMethod.Get, "/actuator/alive")) {
                    assertEquals(HttpStatusCode.OK, response.status())
                }
            }
        }
    }
}
