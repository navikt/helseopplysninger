package no.nav.helse.hops.security

object ResourceLoader {
    /** Loads a file from the class-path as a String, this includes files in the /resources dir. **/
    fun asString(resource: String): String =
        try {
            object {}.javaClass.getResource(resource)!!.readText(Charsets.UTF_8)
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }

    /** Loads a file from the class-path as a ByteArray, this includes files in the /resources dir. **/
    fun asByteArray(resource: String): ByteArray =
        try {
            object {}.javaClass.getResource(resource)!!.readBytes()
        } catch (all: Exception) {
            throw RuntimeException("Failed to load resource=$resource!", all)
        }
}
