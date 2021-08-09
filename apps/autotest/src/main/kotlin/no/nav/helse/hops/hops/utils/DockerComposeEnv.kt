package no.nav.helse.hops.hops.utils

import io.github.cdimascio.dotenv.Dotenv
import io.github.cdimascio.dotenv.dotenv
import io.ktor.http.Url

data class DockerComposeEnv(val dotenv: Dotenv = dotenv { filename = "autotest.env" }) {

    val kafkaBrokers: String = getKafkaOutsideListner(envVar("KAFKA_ADVERTISED_LISTENERS"))
    val discoveryUrl: String = envVar("AZURE_APP_WELL_KNOWN_URL")
    val bestillingTopic: String = envVar("HOPS_BESTILLING_TOPIC")
    val fhirBaseUrl: String = envVarUrl("HAPI_BASE_URL")

    private fun envVar(key: String): String {
        return dotenv[key] ?: throw RuntimeException("Missing required variable \"$key\"")
    }

    private fun envVarUrl(key: String): String {
        return changeUrl(envVar(key))
    }

    fun changeUrl(url: String): String {
        val input = Url(url).copy(host = "localhost")
        return input.toString()
    }

    fun getKafkaOutsideListner(listners: String): String {
        var outsideListner = ""
        listners.split(",").forEach {
            val url = Url(it)
            if (url.protocol.name == "outside") {
                outsideListner = url.host + ":" + url.port
            }
        }
        if (outsideListner == "") {
            throw RuntimeException("Couldn't find outside listner in envVar: \"$listners\"")
        }
        return outsideListner
    }
}
