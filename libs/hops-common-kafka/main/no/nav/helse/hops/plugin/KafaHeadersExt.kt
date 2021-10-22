package no.nav.helse.hops.plugin

import org.apache.kafka.common.header.Headers

operator fun Headers.get(key: String) =
    headers(key).map { it.value().decodeToString() }.single()

operator fun Headers.set(key: String, value: String) {
    remove(key).add(key, value.toByteArray())
}
