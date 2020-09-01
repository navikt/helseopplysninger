package no.nav.helseopplysninger

import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import java.util.concurrent.TimeUnit
import no.nav.helseopplysninger.api.registerNaisApi


data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

val state: ApplicationState = ApplicationState(running = false, initialized = false)
val env: Environment = Environment()

fun main() {
    val server = embeddedServer(Netty, applicationEngineEnvironment {
        log = LoggerFactory.getLogger("ktor.application")

        connector {
            port = env.applicationPort
        }

        module {
            init()
            serverModule()
        }
    })
    Runtime.getRuntime().addShutdownHook(Thread {
        server.stop(10, 10, TimeUnit.SECONDS)
    })

    server.start(wait = false)
}

fun Application.init() {
    state.running = true
}

fun Application.serverModule() {

    routing {
        registerNaisApi(state)
    }

    state.initialized = true
}


fun CoroutineScope.createListener(applicationState: ApplicationState, action: suspend CoroutineScope.() -> Unit): Job =
        launch {
            try {
                action()
            } finally {
                applicationState.running = false
            }
        }


