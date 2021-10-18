package archive

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.http.ContentType
import io.ktor.http.contentType
import java.util.Date

class Dokarkiv(
    private val config: Config.Endpoint,
    private val client: HttpClient
) {
    suspend fun add(doc: Journalpost, correlationId: String) =
        client.post<Unit>("${config.baseUrl}/rest/journalpostapi/v1/journalpost?forsoekFerdigstill=true") {
            contentType(ContentType.Application.Json)
            header("Nav-Callid", correlationId)
            body = doc.toJson()
        }
}

interface Journalpost {
    fun toJson(): String
    enum class Type { INNGAAENDE, UTGAAENDE }
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
              ${if (type == Journalpost.Type.INNGAAENDE) """"datoMottatt": "$datoMottatt",""" else "" }
              "eksternReferanseId": "$eksternReferanseId",
              "tittel": "$tittel",
              "journalpostType": "$type",
              "tema": "$tema",
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
