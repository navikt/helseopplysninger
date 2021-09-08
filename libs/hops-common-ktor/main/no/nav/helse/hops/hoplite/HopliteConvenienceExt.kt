package no.nav.helse.hops.hoplite

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.MapPropertySource
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.parsers.PropsParser
import com.sksamuel.hoplite.yaml.YamlParser
import io.ktor.application.Application
import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import java.io.ByteArrayOutputStream
import java.util.Properties

inline fun <reified T : Any> loadConfigsOrThrow(vararg resources: String = arrayOf("/application.conf")) =
    ConfigLoader.Builder()
        .addShadowJarWorkaround()
        .build()
        .loadConfigOrThrow<T>(*resources)

inline fun <reified T : Any> Application.loadConfigsOrThrow(vararg resources: String = arrayOf("/application.conf")) =
    ConfigLoader.Builder()
        .addShadowJarWorkaround()
        .addKtorConfig(environment.config)
        .build()
        .loadConfigOrThrow<T>(*resources)

/** Used to add the Ktor's MapApplicationConfig as a PropertySource.
 * This allows the MapApplicationConfig to be used to override config values in tests. */
fun ConfigLoader.Builder.addKtorConfig(config: ApplicationConfig) = apply {
    if (config is MapApplicationConfig) {
        // Workaround to access the private property 'map'.
        val map = config.javaClass.getDeclaredField("map").let {
            it.isAccessible = true
            it.get(config) as Map<String, String>
        }

        addPropertySource(MapPropertySource(map))
    }
}

/** For some reason the Parsers needs to be explicitly mapped to file-extensions to work with ShadowJar. */
fun ConfigLoader.Builder.addShadowJarWorkaround() = apply {
    addFileExtensionMapping("properties", PropsParser())
    addFileExtensionMapping("yaml", YamlParser())
}
