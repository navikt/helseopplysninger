package e2e

import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.withTestApplication

fun <R> withTestApp(
    test: TestApplicationEngine.() -> R,
): R = withTestApplication(
    {
        config()
        main()
    },
    test
)

fun Application.config() = (environment.config as MapApplicationConfig).apply {
    put("api.internal.domain", "")
    put("api.github.base-url", Mocks.github.getBaseUrl())
}
