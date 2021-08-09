package no.nav.helse.hops.security

import java.net.URI
import java.util.UUID

fun UUID.toUri() = URI("urn:uuid:$this")
