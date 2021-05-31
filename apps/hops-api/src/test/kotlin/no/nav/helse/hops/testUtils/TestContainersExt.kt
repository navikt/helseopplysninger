@file:Suppress("HttpUrlsUsage")

package no.nav.helse.hops.testUtils

import org.testcontainers.containers.GenericContainer
import java.net.URL

val GenericContainer<*>.url: URL get() = URL("http://$host:$firstMappedPort")
