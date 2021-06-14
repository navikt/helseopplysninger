package no.nav.helse.hops.routes

import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ValidationOptions
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.util.AttributeKey

/** A ktor plugin (formly feature) that intercepts http requests and validates the body, on errors the pipeline
 * is short-circuited and a bad-request response with an operation-outcome is returned. **/
class FhirValidatorKtorPlugin(config: Configuration) {
    private val validator = config.validator!!
    private val options = config.options

    class Configuration {
        var validator: FhirValidator? = null
        var options: ValidationOptions? = null
    }

    // Implemented according to https://ktor.io/docs/creating-custom-plugins.html
    companion object Feature : ApplicationFeature<ApplicationCallPipeline, Configuration, FhirValidatorKtorPlugin> {
        override val key = AttributeKey<FhirValidatorKtorPlugin>(FhirValidatorKtorPlugin::class.java.simpleName)

        override fun install(pipeline: ApplicationCallPipeline, configure: Configuration.() -> Unit): FhirValidatorKtorPlugin {
            val configuration = Configuration().apply(configure)
            val feature = FhirValidatorKtorPlugin(configuration)

            pipeline.intercept(ApplicationCallPipeline.Features) {
                val body = call.receiveText().ifBlank { return@intercept }

                val validationResult = feature.validator.validateWithResult(body, feature.options)
                if (validationResult.isSuccessful) return@intercept proceed()

                val outcome = validationResult.toOperationOutcome()
                call.respond(HttpStatusCode.BadRequest, outcome)
                finish()
            }

            return feature
        }
    }
}
