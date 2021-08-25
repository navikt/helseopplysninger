package eventstore.domain

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.r5.utils.IResourceValidator

object FhirValidatorFactory {
    fun create(): FhirValidator {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)

        val validationSupportChain = ValidationSupportChain(
            DefaultProfileValidationSupport(ctx),
            InMemoryTerminologyServerValidationSupport(ctx),
            CommonCodeSystemsTerminologyService(ctx)
        )

        return ctx.newValidator().apply {
            val module = FhirInstanceValidator(validationSupportChain).apply {
                bestPracticeWarningLevel = IResourceValidator.BestPracticeWarningLevel.Ignore
                isErrorForUnknownProfiles = false
            }
            registerValidatorModule(module)
        }
    }
}
