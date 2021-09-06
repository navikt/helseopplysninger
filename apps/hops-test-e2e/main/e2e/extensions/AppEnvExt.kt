package e2e.extensions

import io.ktor.application.ApplicationEnvironment

inline fun <reified T> ApplicationEnvironment.getRequired(key: String): T =
    config.propertyOrNull(key) as? T ?: error("missing property $key")
