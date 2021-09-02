package no.nav.helse.hops.hoplite

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import com.sksamuel.hoplite.parsers.PropsParser
import io.ktor.application.Application
import io.ktor.config.MapApplicationConfig
import java.io.ByteArrayOutputStream
import java.util.Properties

inline fun <reified T : Any> loadConfigsOrThrow(vararg resources: String = arrayOf("/application.conf")) =
    ConfigLoader.Builder()
        .addFileExtensionMapping("properties", PropsParser()) // For some reason this is needed to work in Docker.
        .build()
        .loadConfigOrThrow<T>(*resources)

inline fun <reified T : Any> Application.loadConfigsOrThrow(vararg resources: String = arrayOf("/application.conf")): T {
    val builder = ConfigLoader
        .Builder()
        .addFileExtensionMapping("properties", PropsParser()) // For some reason this is needed to work in Docker.

    val ktorConfig = environment.config

    if (ktorConfig is MapApplicationConfig) {
        builder.addPropertySources(listOf(ktorConfig.asPropertySource()))
    }

    return builder.build()
        .loadConfigOrThrow(resources.toList())
}

fun MapApplicationConfig.asPropertySource(): PropertySource {
    val props = Properties()

    val map: Map<String, String> = getPrivateProperty("map")
    map.forEach { props.setProperty(it.key, it.value) }

    ByteArrayOutputStream().use { output ->
        props.store(output, "")
        return PropertySource.string(output.toString(), "properties")
    }
}

private inline fun <T : Any, reified R : Any> T.getPrivateProperty(variableName: String): R =
    javaClass.getDeclaredField(variableName).let {
        it.isAccessible = true
        it.get(this) as R
    }
