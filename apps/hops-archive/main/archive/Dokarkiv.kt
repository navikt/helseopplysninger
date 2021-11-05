package archive

import io.ktor.client.HttpClient
import io.ktor.client.features.ClientRequestException
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import no.nav.helse.hops.security.toIsoString
import java.util.Date

class Dokarkiv(
    private val config: Config.Endpoint,
    private val client: HttpClient
) {
    suspend fun add(doc: Journalpost, correlationId: String) {
        try {
            client.post<Unit>("${config.baseUrl}/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true") {
                contentType(ContentType.Application.Json)
                header("Nav-Callid", correlationId)
                body = doc.toJson()
            }
        } catch (ex: ClientRequestException) {
            // Journalpost med eksternReferanseId eksisterer allerede.
            if (ex.response.status != HttpStatusCode.Conflict) {
                throw ex
            }
        }
    }
}

interface Journalpost {
    fun toJson(): String
    enum class Type(private val code: String) {
        INNGAAENDE("I"), UTGAAENDE("U");
        override fun toString() = code
    }
}

/** Dokumentasjon finnes på [confluence](https://confluence.adeo.no/display/BOA/opprettJournalpost). */
@Suppress("SpellCheckingInspection")
class GenericJournalpost(
    val type: Journalpost.Type,
    val datoMottatt: Date,
    val eksternReferanseId: String,
    val tittel: String,
    val tema: String,
    val legeHpr: String,
    val brukerFnr: String,
    val brevkode: String,
    val arkiv: ByteArray,
    val original: ByteArray,
) : Journalpost {
    override fun toJson() =
        """{
              ${if (type == Journalpost.Type.INNGAAENDE) """"datoMottatt": "${datoMottatt.toIsoString()}",""" else "" }
              "eksternReferanseId": "$eksternReferanseId",
              "tittel": "$tittel",
              "journalpostType": "$type",
              "tema": "$tema",
              "kanal": "helseopplysninger",
              "journalfoerendeEnhet": 9999,
              "avsenderMottaker": {
                "id": "$legeHpr",
                "idType": "HPR"
              },
              "bruker": {
                "id": "$brukerFnr",
                "idType": "FNR"
              },
              "sak": {
                "sakstype": "GENERELL_SAK"
              },
              "dokumenter": [
                {
                  "brevkode": "$brevkode",
                  "dokumentvarianter": [
                    {
                      "filtype": "PDFA",
                      "fysiskDokument": "${java.util.Base64.getEncoder().encodeToString(arkiv)}",
                      "variantformat": "ARKIV"
                    },
                    {
                      "filtype": "JSON",
                      "fysiskDokument": "${java.util.Base64.getEncoder().encodeToString(original)}",
                      "variantformat": "ORIGINAL"
                    }
                  ]
                }
              ]
            }
        """.trimIndent()
}
