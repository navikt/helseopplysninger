package no.nav.helseopplysninger

import io.ktor.application.Application
import io.ktor.routing.routing
import io.ktor.server.engine.*
import io.ktor.server.netty.Netty
import kotlinx.coroutines.*
import io.ktor.config.HoconApplicationConfig
import com.typesafe.config.ConfigFactory
import java.util.concurrent.TimeUnit
import no.nav.helseopplysninger.api.registerNaisApi
import no.nav.helseopplysninger.db.DatabaseConfig
import no.nav.helseopplysninger.db.DatabaseInterface
import no.nav.helseopplysninger.db.Database


data class ApplicationState(var running: Boolean = true, var initialized: Boolean = false)

val state: ApplicationState = ApplicationState(running = false, initialized = false)
val env: Environment = getEnvironment()
lateinit var database: DatabaseInterface

fun main() {
    val server = embeddedServer(Netty, applicationEngineEnvironment {
        config = HoconApplicationConfig(ConfigFactory.load())

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

// TODO: Check when proxySql container is ready to avoid initial application crash

fun Application.init() {
    isRemote {
        database = Database(DatabaseConfig(
                jdbcUrl = env.jdbcUrl(),
                username = env.dbUsername,
                password = env.dbPassword,
                databaseName = env.dbName))
        state.running = true
    }

    isLocal {
        // TODO: Setup postgres instance when running locally (docker-compose?)
        state.running = true
    }
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

val Application.envKind get() = environment.config.property("ktor.environment").getString()

fun Application.isLocal(block: () -> Unit) {
    if (envKind == "local") block()
}

fun Application.isRemote(block: () -> Unit) {
    if (envKind == "remote") block()
}
