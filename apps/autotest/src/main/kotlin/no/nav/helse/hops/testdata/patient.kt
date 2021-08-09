package no.nav.helse.hops.testdata

import no.nav.helse.hops.fhir.Constants
import no.nav.helse.hops.utils.fnr.FoedselsnummerGenerator
import no.nav.helse.hops.utils.fnr.Kjoenn
import org.hl7.fhir.r4.model.BooleanType
import org.hl7.fhir.r4.model.Enumerations.AdministrativeGender
import org.hl7.fhir.r4.model.Identifier
import org.hl7.fhir.r4.model.Patient
import java.time.LocalDate
import java.time.ZoneId
import java.util.Date
import java.util.UUID
import kotlin.random.Random

/**
 * Generer en norsk pasient med random
 */
fun createPatient(): Patient {
    val patient = Patient()

    val youngest = LocalDate.now().toEpochDay() - 1
    val oldest = youngest - (365 * 120)
    val seedDate = LocalDate.ofEpochDay(Random.nextInt(oldest.toInt(), youngest.toInt()).toLong())

    val fnr = FoedselsnummerGenerator().foedselsnummer(seedDate)
    patient.active = true
    patient.birthDate = Date.from(fnr.foedselsdato.atStartOfDay(ZoneId.systemDefault()).toInstant())
    patient.id = UUID.randomUUID().toString()
    val identifier = Identifier()
    identifier.value = fnr.asString
    identifier.use = Identifier.IdentifierUse.OFFICIAL
    identifier.system = Constants.OID_FNR
    patient.addIdentifier(identifier)
    patient.deceased = BooleanType(false)
    patient.gender = if (fnr.kjoenn == Kjoenn.MANN) AdministrativeGender.MALE else AdministrativeGender.FEMALE
    return patient
}
