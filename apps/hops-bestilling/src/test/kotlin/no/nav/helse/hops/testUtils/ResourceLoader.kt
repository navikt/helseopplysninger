package no.nav.helse.hops.testUtils

internal object ResourceLoader {
    fun asString(resource: String): String =
        try {
            object {}.javaClass.getResource(resource)!!.readText(Charsets.UTF_8)
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }

    fun asByteArray(resource: String): ByteArray =
        try {
            object {}.javaClass.getResource(resource)!!.readBytes()
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }
}
