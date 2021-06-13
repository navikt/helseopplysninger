package no.nav.helse.hops.routes

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import ca.uhn.fhir.validation.ValidationOptions
import io.ktor.application.ApplicationCallPipeline
import io.ktor.application.ApplicationFeature
import io.ktor.application.call
import io.ktor.http.HttpStatusCode
import io.ktor.request.receiveText
import io.ktor.response.respond
import io.ktor.util.AttributeKey
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.r5.utils.IResourceValidator

/** A ktor plugin (formly feature) that intercepts http requests and validates the body, on errors the pipeline
 * is short-circuited and a bad request response with an operation-outcome is returned. **/
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
                if (!validationResult.isSuccessful) {
                    val outcome = validationResult.toOperationOutcome()
                    call.respond(HttpStatusCode.BadRequest, outcome)
                    finish() // https://ktor.io/docs/intercepting-routes.html#how-to-intercept-preventing-additional-executions
                }
            }
            return feature
        }

        val r4Validator: FhirValidator by lazy {
            val ctx = FhirContext.forCached(FhirVersionEnum.R4)

            val validationSupportChain = ValidationSupportChain(
                DefaultProfileValidationSupport(ctx),
                InMemoryTerminologyServerValidationSupport(ctx),
                CommonCodeSystemsTerminologyService(ctx)
            )

            ctx.newValidator().apply {
                val module = FhirInstanceValidator(validationSupportChain).apply {
                    bestPracticeWarningLevel = IResourceValidator.BestPracticeWarningLevel.Ignore
                    isErrorForUnknownProfiles = false
                }
                registerValidatorModule(module)
            }
        }
    }
}
