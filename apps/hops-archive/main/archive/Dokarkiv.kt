package archive

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.Date

class Dokarkiv(
    private val config: Config.Endpoint,
    private val client: HttpClient
) {
    suspend fun add(doc: Journalpost, forsoekFerdigstill: Boolean) =
        client.post<Unit>("${config.baseUrl}/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=$forsoekFerdigstill") {
            contentType(ContentType.Application.Json)
            body = doc.toJson()
        }
}

interface Journalpost {
    fun toJson(): String
}

/** Dokumentasjon finnes på [confluence](https://confluence.adeo.no/display/BOA/opprettJournalpost). */
@Suppress("SpellCheckingInspection")
class InngaaendeJournalpost(
    val datoMottatt: Date,
    val eksternReferanseId: String,
    val tittel: String,
    val legeHpr: String,
    val brukerFnr: String,
    val brevkode: String,
    val arkiv: ByteArray,
    val original: ByteArray,
) : Journalpost {
    override fun toJson() =
        """{
              "datoMottatt": "$datoMottatt",
              "eksternReferanseId": "$eksternReferanseId",
              "tittel": "$tittel",
              "journalpostType": "INNGAAENDE",
              "tema": "DAG",
              "kanal": "helseopplysninger",
              "journalfoerendeEnhet": 9999,
              "avsenderMottaker": {
                "id": "$legeHpr",
                "idType": "HPR",
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
                  ],
                }
              ]
            }
        """.trimIndent()
}
