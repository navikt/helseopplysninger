package no.nav.helseopplysninger

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import java.io.File

const val DB_CONFIG_ENV_PREFIX = "NAIS_DATABASE_HELSEOPPLYSNINGER_HELSEOPPLYSNINGERDB"
const val localEnvironmentPropertiesPath = "./src/main/resources/localEnv.json"
private val objectMapper: ObjectMapper = ObjectMapper()

data class Environment(
        val applicationPort: Int,
        val applicationThreads: Int,
        val dbUrl: String,
        val dbHost: String,
        val dbPort: Int,
        val dbName: String,
        val dbUsername: String,
        val dbPassword: String
)

fun getEnvironment(): Environment {
    objectMapper.registerKotlinModule()
    return if (appIsRunningLocally) {
        objectMapper.readValue(File(localEnvironmentPropertiesPath), Environment::class.java)
    } else {
        Environment(
                getEnvVar("APPLICATION_PORT", "8080").toInt(),
                getEnvVar("APPLICATION_THREADS", "4").toInt(),
                getEnvVar("${DB_CONFIG_ENV_PREFIX}_URL"),
                getEnvVar("${DB_CONFIG_ENV_PREFIX}_HOST"),
                getEnvVar("${DB_CONFIG_ENV_PREFIX}_PORT").toInt(),
                getEnvVar("${DB_CONFIG_ENV_PREFIX}_DATABASE"),
                getEnvVar("${DB_CONFIG_ENV_PREFIX}_USERNAME"),
                getEnvVar("${DB_CONFIG_ENV_PREFIX}_PASSWORD")
        )
    }
}

fun Environment.jdbcUrl(): String {
    return "jdbc:postgresql://$dbHost:$dbPort/$dbName"
}

val appIsRunningLocally: Boolean = System.getenv("NAIS_CLUSTER_NAME").isNullOrEmpty()

fun getEnvVar(varName: String, defaultValue: String? = null) =
        System.getenv(varName) ?: defaultValue ?: throw RuntimeException("Missing required variable \"$varName\"")
