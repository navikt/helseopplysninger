package no.nav.helse.hops.hops.testdata

import no.nav.helse.hops.hops.fhir.toJson
import org.junit.jupiter.api.Test

internal class QuestionnaireKtTest {

    @Test
    fun createQuestionnaireTest() {
        val q = createQuestionnaire()
        println(q.toJson())
    }
}
