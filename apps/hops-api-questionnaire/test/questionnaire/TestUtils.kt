package questionnaire

import com.sksamuel.hoplite.ConfigLoader
import com.sksamuel.hoplite.yaml.YamlPropertySource
import io.ktor.server.testing.TestApplicationResponse
import no.nav.helse.hops.fhir.JsonConverter
import org.hl7.fhir.r4.model.Bundle
import org.hl7.fhir.r4.model.DomainResource
import java.io.File

fun resourceFile(path: String): String = object {}.javaClass.getResource(path)!!.readText()

inline fun <reified T : DomainResource> resourceFile(path: String): T = JsonConverter.parse(resourceFile(path))

fun String.replaceHandlebar(port: Int): String = replace("{{ port }}", "$port")

val projectPath: String = File("").absolutePath

/**
 * replace {{ anything }} with "dummy"
 * Hoplite do not support handlebars and in test we do not care about these.
 */
fun String.removeHandlebars(): String = replace(Regex("(?=\\{\\{).*?}}"), "dummy")

fun TestApplicationResponse.bodyAsBundle(): Bundle = JsonConverter.parse(content!!)

inline fun <reified T : Any> yaml(absoluteFilePath: String): T =
    ConfigLoader.Builder()
        .addSource(YamlPropertySource(File(absoluteFilePath).readText().removeHandlebars()))
        .build()
        .loadConfigOrThrow()

data class Nais(val spec: Spec)
data class Spec(val liveness: Health, val readiness: Health, val prometheus: Prometheus)
data class Health(val path: String)
data class Prometheus(val path: String)
