package no.nav.helseopplysninger

import io.ktor.http.*
import io.ktor.routing.routing
import io.ktor.server.testing.TestApplicationEngine
import io.ktor.server.testing.handleRequest
import no.nav.helseopplysninger.api.registerNaisApi
import org.amshove.kluent.shouldEqual
import org.amshove.kluent.shouldNotEqual
import org.spekframework.spek2.Spek
import org.spekframework.spek2.style.specification.describe
import java.net.ServerSocket

object SelftestSpek : Spek({
    fun getRandomPort() = ServerSocket(0).use {
        it.localPort
    }

    val applicationState = ApplicationState()

    describe("Calling selftest with successful liveness and readyness tests") {
        with(TestApplicationEngine()) {
            start()
            application.routing {
                registerNaisApi(applicationState)
            }

            it("Returns OK on isAlive") {
                applicationState.running = true

                with(handleRequest(HttpMethod.Get, "/isAlive")) {
                    response.status()?.isSuccess() shouldEqual true
                    response.content shouldNotEqual null
                }
            }
            it("Returns OK on isReady") {
                applicationState.initialized = true

                with(handleRequest(HttpMethod.Get, "/isReady")) {
                    response.status()?.isSuccess() shouldEqual true
                    response.content shouldNotEqual null
                }
            }
            it("Returns error on failed isAlive") {
                applicationState.running = false

                with(handleRequest(HttpMethod.Get, "/isAlive")) {
                    response.status()?.isSuccess() shouldNotEqual true
                    response.content shouldNotEqual null
                }
            }
            it("Returns error on failed isReady") {
                applicationState.initialized = false

                with(handleRequest(HttpMethod.Get, "/isReady")) {
                    response.status()?.isSuccess() shouldNotEqual true
                    response.content shouldNotEqual null
                }
            }
        }
    }

    describe("Calling selftests with unsuccessful liveness test") {
        with(TestApplicationEngine()) {
            start()
            application.routing {
                registerNaisApi(ApplicationState(running = false))
            }

            it("Returns internal server error when liveness check fails") {
                with(handleRequest(HttpMethod.Get, "/isAlive")) {
                    response.status() shouldEqual HttpStatusCode.InternalServerError
                    response.content shouldNotEqual null
                }
            }
        }
    }

    describe("Calling selftests with unsucessful readyness test") {
        with(TestApplicationEngine()) {
            start()
            application.routing {
                registerNaisApi(ApplicationState(initialized = false))
            }

            it("Returns internal server error when readyness check fails") {
                with(handleRequest(HttpMethod.Get, "/isReady")) {
                    response.status() shouldEqual HttpStatusCode.InternalServerError
                    response.content shouldNotEqual null
                }
            }
        }
    }
})
