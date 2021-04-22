package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.context.support.DefaultProfileValidationSupport
import ca.uhn.fhir.validation.FhirValidator
import no.nav.helse.hops.domain.FhirResourceValidator
import org.hl7.fhir.common.hapi.validation.support.CommonCodeSystemsTerminologyService
import org.hl7.fhir.common.hapi.validation.support.InMemoryTerminologyServerValidationSupport
import org.hl7.fhir.common.hapi.validation.support.ValidationSupportChain
import org.hl7.fhir.common.hapi.validation.validator.FhirInstanceValidator
import org.hl7.fhir.instance.model.api.IBaseResource
import org.hl7.fhir.r4.model.OperationOutcome

object FhirResourceValidatorHapi : FhirResourceValidator {
    private val validator: FhirValidator

    init {
        val ctx = FhirContext.forCached(FhirVersionEnum.R4)

        val validationSupportChain = ValidationSupportChain(
            DefaultProfileValidationSupport(ctx),
            InMemoryTerminologyServerValidationSupport(ctx),
            CommonCodeSystemsTerminologyService(ctx)
        )

        val module = FhirInstanceValidator(validationSupportChain)

        validator = ctx.newValidator()
        validator.registerValidatorModule(module)
    }

    override suspend fun validate(resource: IBaseResource): OperationOutcome {
        val result = validator.validateWithResult(resource)
        return result.toOperationOutcome() as OperationOutcome
    }
}
