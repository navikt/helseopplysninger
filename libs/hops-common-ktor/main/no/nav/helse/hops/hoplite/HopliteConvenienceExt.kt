package no.nav.helse.hops.hoplite

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.MapPropertySource
import com.sksamuel.hoplite.parsers.PropsParser
import io.ktor.application.Application
import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig

inline fun <reified T : Any> loadConfigsOrThrow(vararg resources: String = arrayOf("/application.conf")) =
    ConfigLoader.Builder()
        .addFileExtensionMapping("properties", PropsParser()) // For some reason this is needed to work in Docker.
        .build()
        .loadConfigOrThrow<T>(*resources)

inline fun <reified T : Any> Application.loadConfigsOrThrow(vararg resources: String = arrayOf("/application.conf")) =
    ConfigLoader.Builder()
        .addFileExtensionMapping("properties", PropsParser()) // For some reason this is needed to work in Docker.
        .addKtorConfig(environment.config)
        .build()
        .loadConfigOrThrow<T>(*resources)

/** Used to add the Ktor's MapApplicationConfig as a PropertySource.
 * This allows the MapApplicationConfig to be used to override config values in tests. */
fun ConfigLoader.Builder.addKtorConfig(ktorConfig: ApplicationConfig) = apply {
    if (ktorConfig is MapApplicationConfig) {
        // Workaround to access the private property 'map'.
        val map = javaClass.getDeclaredField("map").let {
            it.isAccessible = true
            it.get(this) as Map<String, String>
        }

        addPropertySource(MapPropertySource(map))
    }
}
