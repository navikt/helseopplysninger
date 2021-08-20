package no.nav.helse.hops.hoplite

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.parsers.PropsParser

inline fun <reified T : Any> loadConfigsOrThrow(resources: List<String> = listOf("/application.conf")) =
    ConfigLoader.Builder()
        .addFileExtensionMapping("properties", PropsParser()) // For some reason this is needed to work in Docker.
        .build()
        .loadConfigOrThrow<T>(resources)
