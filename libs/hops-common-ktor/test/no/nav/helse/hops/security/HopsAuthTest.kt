package no.nav.helse.hops.security

import com.nimbusds.jwt.SignedJWT
import io.ktor.application.Application
import io.ktor.application.call
import io.ktor.application.install
import io.ktor.auth.authenticate
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode
import io.ktor.response.respondText
import io.ktor.routing.get
import io.ktor.routing.routing
import io.ktor.server.testing.handleRequest
import io.ktor.server.testing.withTestApplication
import kotlin.test.assertEquals
import no.nav.helse.hops.MockServers
import no.nav.helse.hops.test.HopsOAuthMock
import org.junit.jupiter.api.DynamicTest
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.TestFactory
import org.junit.jupiter.api.TestInstance
import org.junit.jupiter.api.assertThrows

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class HopsAuthTest {
    init {
        MockServers.oAuth.start()
    }

    @Test
    fun `It should fail when installing with incomplete config`() {
        val e = assertThrows<IllegalStateException> {
            withTestApplication(
                {
                    install(HopsAuth)
                },
                {}
            )
        }

        assertEquals("No authentication configuration provided!", e.message)
    }

    @Test
    fun `It should have default and explicit auth realms when only azure is setup`() {
        withTestApplication(Application::azureOnly) {
            var call = handleRequest(HttpMethod.Get, "/") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            assertEquals(HttpStatusCode.OK, call.response.status())

            call = handleRequest(HttpMethod.Get, "/explicit") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            assertEquals(HttpStatusCode.OK, call.response.status())
        }
    }

    @Test
    fun `It should respond unauthorized when only azure is setup and no_bad token is sent`() {
        withTestApplication(Application::azureOnly) {
            var call = handleRequest(HttpMethod.Get, "/")

            assertEquals(HttpStatusCode.Unauthorized, call.response.status())

            call = handleRequest(HttpMethod.Get, "/") {
                addHeader(HttpHeaders.Authorization, "Bearer FAKE")
            }

            assertEquals(HttpStatusCode.Unauthorized, call.response.status())
        }
    }

    @TestFactory
    fun `It should respect realm rules`(): List<DynamicTest> {
        class Row(val endpoint: String, val token: SignedJWT, val expectedStatus: HttpStatusCode)

        val rows = listOf(
            Row("/read", MockServers.oAuth.issueMaskinportenToken(), HttpStatusCode.OK),
            Row(
                "/read",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.OK
            ),
            Row(
                "/read",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/read",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.OK
            ),
            Row(
                "/write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/write",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/azure",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.OK
            ),
            Row(
                "/azure",
                MockServers.oAuth.issueMaskinportenToken(), // both scopes
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/read-or-write",
                MockServers.oAuth.issueMaskinportenToken(), // both scopes
                HttpStatusCode.OK
            ),
            Row(
                "/read-or-write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.OK
            ),
            Row(
                "/read-or-write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.OK
            ),
            Row(
                "/read-or-write",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.Unauthorized
            ),
            Row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueMaskinportenToken(), // both scopes
                HttpStatusCode.OK
            ),
            Row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.OK
            ),
            Row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.OK
            ),
            Row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.OK
            )
        )

        return rows.map {
            val displayName = it.run { """Calling $endpoint with token ${token.jwtClaimsSet.issuer.substringAfterLast("/")}:scopes="${token.jwtClaimsSet.claims["scope"]}" should return $expectedStatus""" }
            DynamicTest.dynamicTest(displayName) {
                withTestApplication(Application::maskinportenAndAzure) {
                    val call = handleRequest(HttpMethod.Get, it.endpoint) {
                        addHeader(HttpHeaders.Authorization, "Bearer ${it.token.serialize()}")
                    }

                    assertEquals(it.expectedStatus, call.response.status())
                }
            }
        }
    }

    @Test
    fun `It should be able to extract auth identity`() {
        withTestApplication(Application::azureOnly) {
            val call = handleRequest(HttpMethod.Get, "/auth-identity") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            assertEquals(HttpStatusCode.OK, call.response.status())

            assertThrows<IllegalStateException> {
                handleRequest(HttpMethod.Get, "/auth-identity-no-auth") {
                    val token = MockServers.oAuth.issueAzureToken()
                    addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                }
            }
        }

        withTestApplication(Application::maskinportenOnly) {
            val orgNumber = "123456789"
            val call = handleRequest(HttpMethod.Get, "/auth-identity") {
                val token = MockServers.oAuth.issueMaskinportenToken(orgNumber)
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            assertEquals(HttpStatusCode.OK, call.response.status())
            assertEquals(orgNumber, call.response.content)

            assertThrows<IllegalStateException> {
                handleRequest(HttpMethod.Get, "/auth-identity-no-auth") {
                    val token = MockServers.oAuth.issueMaskinportenToken()
                    addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                }
            }
        }

        withTestApplication(Application::maskinportenAndAzure) {
            val orgNumber = "123456789"
            var call = handleRequest(HttpMethod.Get, "/auth-identity") {
                val token = MockServers.oAuth.issueMaskinportenToken(orgNumber)
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            assertEquals(HttpStatusCode.OK, call.response.status())
            assertEquals(orgNumber, call.response.content)

            call = handleRequest(HttpMethod.Get, "/auth-identity") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            assertEquals(HttpStatusCode.OK, call.response.status())
            assertEquals("azure", call.response.content)

            assertThrows<IllegalStateException> {
                handleRequest(HttpMethod.Get, "/auth-identity-no-auth") {
                    val token = MockServers.oAuth.issueMaskinportenToken()
                    addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                }
            }

            assertThrows<IllegalStateException> {
                handleRequest(HttpMethod.Get, "/auth-identity-no-auth") {
                    val token = MockServers.oAuth.issueAzureToken()
                    addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                }
            }
        }
    }
}

fun Application.azureOnly() {
    install(HopsAuth) {
        providers += AzureADProvider(MockServers.oAuth.buildAzureConfig())
    }

    routing {
        authenticate {
            get {
                call.respondText("ok")
            }
            get("/auth-identity") {
                val azure = call.authIdentityAzure()
                call.respondText("Extracted azure identity: $azure")
            }
        }
        authenticate(AzureADProvider.REALM) {
            get("/explicit") {
                call.respondText("ok")
            }
        }
        get("/auth-identity-no-auth") {
            val azure = call.authIdentityAzure()
            call.respondText("Extracted azure identity: $azure")
        }
    }
}

fun Application.maskinportenOnly() {
    install(HopsAuth) {
        providers += MaskinportenProvider(MockServers.oAuth.buildMaskinportenConfig())
    }

    routing {
        authenticate(MaskinportenProvider.READ_REALM) {
            get("/read") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.WRITE_REALM) {
            get("/write") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.READ_REALM, MaskinportenProvider.WRITE_REALM) {
            get("/read-or-write") {
                call.respondText("ok")
            }
            get("/auth-identity") {
                val identity = call.authIdentityMaskinporten()
                call.respondText(identity.orgNr)
            }
        }
        get("/auth-identity-no-auth") {
            val identity = call.authIdentityMaskinporten()
            call.respondText(identity.orgNr)
        }
    }
}

fun Application.maskinportenAndAzure() {
    install(HopsAuth) {
        providers += MaskinportenProvider(MockServers.oAuth.buildMaskinportenConfig())
        providers += AzureADProvider(MockServers.oAuth.buildAzureConfig())
    }

    routing {
        authenticate(AzureADProvider.REALM) {
            get("/azure") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.READ_REALM) {
            get("/read") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.READ_REALM, AzureADProvider.REALM) {
            get("/read-or-azure") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.WRITE_REALM) {
            get("/write") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.WRITE_REALM, AzureADProvider.REALM) {
            get("/write-or-azure") {
                call.respondText("ok")
            }
        }
        authenticate(MaskinportenProvider.READ_REALM, MaskinportenProvider.WRITE_REALM) {
            get("/read-or-write") {
                call.respondText("ok")
            }
        }
        authenticate(
            MaskinportenProvider.READ_REALM,
            MaskinportenProvider.WRITE_REALM,
            AzureADProvider.REALM
        ) {
            get("/read-or-write-or-azure") {
                call.respondText("ok")
            }
            get("/auth-identity") {
                when (val identity = call.authIdentity()) {
                    is MaskinportenProvider.MaskinportenIdentity -> call.respondText(identity.orgNr)
                    is AzureADProvider.AzureADIdentity -> call.respondText("azure")
                }
            }
        }
        get("/auth-identity-no-auth") {
            val identity = call.authIdentity()
            call.respondText("should fail to get identity: $identity")
        }
    }
}
