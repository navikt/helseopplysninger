package no.nav.helse.hops

import java.net.URI
import java.util.UUID

fun UUID.toUri() = URI("urn:uuid:$this")
