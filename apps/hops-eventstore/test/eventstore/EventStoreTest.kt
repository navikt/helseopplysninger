package eventstore

import eventstore.infrastructure.Config
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.config.AbstractProjectConfig
import io.kotest.core.listeners.Listener
import io.kotest.core.listeners.ProjectListener
import io.kotest.core.spec.style.FeatureSpec
import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.Serializable
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import no.nav.helse.hops.hoplite.loadConfigsOrThrow
import no.nav.security.mock.oauth2.MockOAuth2Server
import org.flywaydb.core.internal.exception.FlywaySqlException
import org.jetbrains.exposed.sql.transactions.transaction

@ExperimentalSerializationApi
internal class EventStoreTest : FeatureSpec({

    feature("GET /fhir/4.0/Bundle") {
        scenario("with token should return bundle") {
            withTestApplication(
                {
                    oAuthConfig()
                    h2Config()
                    main()
                }
            ) {
                with(
                    handleRequest(HttpMethod.Get, "/fhir/4.0/Bundle") {
                        addHeader("Authorization", "Bearer ${oAuthMock.issueToken().serialize()}")
                    }
                ) {
                    response shouldHaveStatus HttpStatusCode.OK
                    response.content shouldNotBe null

                    Json.decodeFromString<TestBundle>(response.content!!).resourceType shouldBe "Bundle"
                }
            }
        }
    }
})

internal class DbConfigTest : StringSpec({

    "without url should be postgres" {
        val config = loadConfigsOrThrow<Config>()
        config.db.shouldBeInstanceOf<Config.Database.Postgres>()
    }

    "sanity url should be postgres" {
        val exception = shouldThrow<FlywaySqlException> {
            withTestApplication(
                {
                    oAuthConfig()
                    main()
                }
            ) {
                // do nothing
            }
        }
        exception.message shouldContain "jdbc:postgresql://localhost:5432/postgres"
    }

    "url should be h2" {
        withTestApplication(
            {
                oAuthConfig()
                h2Config()
                main()
            }
        ) {
            transaction {
                db.url shouldBe "jdbc:h2:mem:test"
            }
        }
    }
})

private fun Application.oAuthConfig() {
    (environment.config as MapApplicationConfig).apply {
        put("no.nav.security.jwt.issuers.size", "1")
        put("no.nav.security.jwt.issuers.0.issuer_name", "default")
        put("no.nav.security.jwt.issuers.0.discoveryurl", "${oAuthMock.wellKnownUrl("default")}")
        put("no.nav.security.jwt.issuers.0.accepted_audience", "default")
    }
}

private fun Application.h2Config() {
    (environment.config as MapApplicationConfig).apply {
        put("db.url", "jdbc:h2:mem:test;DB_CLOSE_DELAY=-1")
    }
}

@Serializable
data class TestBundle(
    val resourceType: String,
    val id: String,
    val type: String,
    val timestamp: String,
)

val oAuthMock = MockOAuth2Server()
internal fun startOAuth() = with(oAuthMock, MockOAuth2Server::start)
internal fun stopOAuth() = with(oAuthMock, MockOAuth2Server::shutdown)

internal class KotestSetup() : AbstractProjectConfig() {
    override fun listeners(): List<Listener> = super.listeners() + KotestListener()
}

internal class KotestListener : ProjectListener {
    override suspend fun beforeProject() = startOAuth()
    override suspend fun afterProject() = stopOAuth()
}
