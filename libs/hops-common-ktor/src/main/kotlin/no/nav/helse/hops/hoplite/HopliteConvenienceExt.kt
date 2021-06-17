package no.nav.helse.hops.hoplite

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.PropertySource
import io.ktor.config.ApplicationConfig
import io.ktor.config.MapApplicationConfig
import org.koin.core.module.Module
import org.koin.core.scope.Scope
import org.koin.dsl.module
import java.io.ByteArrayOutputStream
import java.util.Properties

/** Uses Hoplite to load and parse configuration file and combines it with registered PropertySources. **/
inline fun <reified T : Any> Scope.loadConfigOrThrow(resource: String = "/application.conf") =
    loadConfigsOrThrow<T>(resource)

inline fun <reified T : Any> Scope.loadConfigsOrThrow(vararg resources: String) =
    ConfigLoader.Builder().addPropertySources(getAll()).build().loadConfigOrThrow<T>(resources.toList())

/** Use this to register MapApplicationConfig as a Hops PropertySource in a Koin Module. **/
fun ApplicationConfig.asHoplitePropertySourceModule(): Module {
    val appConfig = this
    return module { if (appConfig is MapApplicationConfig) single { appConfig.asPropertySource() } }
}

private fun MapApplicationConfig.asPropertySource(): PropertySource {
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
