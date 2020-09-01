package no.nav.helseopplysninger

data class Environment(
        val applicationPort: Int = getEnvVar("APPLICATION_PORT", "8080").toInt(),
        val applicationThreads: Int = getEnvVar("APPLICATION_THREADS", "4").toInt(),
        val srvappnameUsername: String = getEnvVar("SRVAPPNAME_USERNAME"),
        val srvappnamePassword: String = getEnvVar("SRVAPPNAME_PASSWORD")
)

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")