package no.nav.helse.hops.security

import io.kotest.assertions.ktor.shouldHaveContent
import io.kotest.assertions.ktor.shouldHaveStatus
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FreeSpec
import io.kotest.data.forAll
import io.kotest.data.headers
import io.kotest.data.row
import io.kotest.data.table
import io.kotest.matchers.shouldBe
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
import no.nav.helse.hops.MockServers
import no.nav.helse.hops.test.HopsOAuthMock

class HopsAuthTest : FreeSpec({

    "It should fail when installing with incomplete config" {
        val e = shouldThrow<IllegalArgumentException> {
            withTestApplication(
                {
                    install(HopsAuth) {
                        azureAD = null
                        maskinporten = null
                    }
                },
                {}
            )
        }

        e.message shouldBe "No authentication configuration provided!"
    }

    "It should have default and explicit auth realms when only azure is setup" {
        withTestApplication(Application::azureOnly) {
            var call = handleRequest(HttpMethod.Get, "/") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            call.response shouldHaveStatus HttpStatusCode.OK

            call = handleRequest(HttpMethod.Get, "/explicit") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            call.response shouldHaveStatus HttpStatusCode.OK
        }
    }

    "It should respond unauthorized when only azure is setup and no/bad token is sent" {
        withTestApplication(Application::azureOnly) {
            var call = handleRequest(HttpMethod.Get, "/")

            call.response shouldHaveStatus HttpStatusCode.Unauthorized

            call = handleRequest(HttpMethod.Get, "/") {
                addHeader(HttpHeaders.Authorization, "Bearer FAKE")
            }

            call.response shouldHaveStatus HttpStatusCode.Unauthorized
        }
    }

    "It should respect realm rules" - {
        table(
            headers("endpoint", "token", "expectedStatus"),
            row("/read", MockServers.oAuth.issueMaskinportenToken(), HttpStatusCode.OK),
            row(
                "/read",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.OK
            ),
            row(
                "/read",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/read",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.OK
            ),
            row(
                "/write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/write",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/azure",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.OK
            ),
            row(
                "/azure",
                MockServers.oAuth.issueMaskinportenToken(), // both scopes
                HttpStatusCode.Unauthorized
            ),
            row(
                "/azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/read-or-write",
                MockServers.oAuth.issueMaskinportenToken(), // both scopes
                HttpStatusCode.OK
            ),
            row(
                "/read-or-write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.OK
            ),
            row(
                "/read-or-write",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.OK
            ),
            row(
                "/read-or-write",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.Unauthorized
            ),
            row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueMaskinportenToken(), // both scopes
                HttpStatusCode.OK
            ),
            row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.READ)),
                HttpStatusCode.OK
            ),
            row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueMaskinportenToken(scopes = setOf(HopsOAuthMock.MaskinportenScopes.WRITE)),
                HttpStatusCode.OK
            ),
            row(
                "/read-or-write-or-azure",
                MockServers.oAuth.issueAzureToken(),
                HttpStatusCode.OK
            ),
        ).forAll { endpoint, token, expectedStatus ->
            """Calling $endpoint with token ${token.jwtClaimsSet.issuer.substringAfterLast("/")}:scopes="${token.jwtClaimsSet.claims["scope"]}" should return $expectedStatus""" {
                withTestApplication(Application::maskinportenAndAzure) {
                    val call = handleRequest(HttpMethod.Get, endpoint) {
                        addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                    }

                    call.response shouldHaveStatus expectedStatus
                }
            }
        }
    }

    "It should be able to extract auth identity" {
        withTestApplication(Application::azureOnly) {
            val call = handleRequest(HttpMethod.Get, "/auth-identity") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            call.response shouldHaveStatus HttpStatusCode.OK

            shouldThrow<IllegalStateException> {
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

            call.response shouldHaveStatus HttpStatusCode.OK
            call.response shouldHaveContent orgNumber

            shouldThrow<IllegalStateException> {
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

            call.response shouldHaveStatus HttpStatusCode.OK
            call.response shouldHaveContent orgNumber

            call = handleRequest(HttpMethod.Get, "/auth-identity") {
                val token = MockServers.oAuth.issueAzureToken()
                addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
            }

            call.response shouldHaveStatus HttpStatusCode.OK
            call.response shouldHaveContent "azure"

            shouldThrow<IllegalStateException> {
                handleRequest(HttpMethod.Get, "/auth-identity-no-auth") {
                    val token = MockServers.oAuth.issueMaskinportenToken()
                    addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                }
            }

            shouldThrow<IllegalStateException> {
                handleRequest(HttpMethod.Get, "/auth-identity-no-auth") {
                    val token = MockServers.oAuth.issueAzureToken()
                    addHeader(HttpHeaders.Authorization, "Bearer ${token.serialize()}")
                }
            }
        }
    }
})

fun Application.azureOnly() {
    install(HopsAuth) {
        azureAD = MockServers.oAuth.buildAzureConfig()
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
        authenticate(HopsAuth.Realms.azureAd.name) {
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
        maskinporten = MockServers.oAuth.buildMaskinportenConfig()
    }

    routing {
        authenticate(HopsAuth.Realms.maskinportenRead.name) {
            get("/read") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenWrite.name) {
            get("/write") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenRead.name, HopsAuth.Realms.maskinportenWrite.name) {
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
        maskinporten = MockServers.oAuth.buildMaskinportenConfig()
        azureAD = MockServers.oAuth.buildAzureConfig()
    }

    routing {
        authenticate(HopsAuth.Realms.azureAd.name) {
            get("/azure") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenRead.name) {
            get("/read") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenRead.name, HopsAuth.Realms.azureAd.name) {
            get("/read-or-azure") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenWrite.name) {
            get("/write") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenWrite.name, HopsAuth.Realms.azureAd.name) {
            get("/write-or-azure") {
                call.respondText("ok")
            }
        }
        authenticate(HopsAuth.Realms.maskinportenRead.name, HopsAuth.Realms.maskinportenWrite.name) {
            get("/read-or-write") {
                call.respondText("ok")
            }
        }
        authenticate(
            HopsAuth.Realms.maskinportenRead.name,
            HopsAuth.Realms.maskinportenWrite.name,
            HopsAuth.Realms.azureAd.name
        ) {
            get("/read-or-write-or-azure") {
                call.respondText("ok")
            }
            get("/auth-identity") {
                when (val identity = call.authIdentity()) {
                    is HopsAuth.AuthIdentity.Maskinporten -> call.respondText(identity.orgNr)
                    is HopsAuth.AuthIdentity.AzureAD -> call.respondText("azure")
                }
            }
        }
        get("/auth-identity-no-auth") {
            val identity = call.authIdentity()
            call.respondText("should fail to get identity: $identity")
        }
    }
}
