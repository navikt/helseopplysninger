package archive.domain

import java.util.Date
import java.util.UUID

/** Dokumentasjon finnes på [confluence](https://confluence.adeo.no/display/BOA/Arkivering+i+fagarkivet#) og [swagger](https://confluence.adeo.no/display/BOA/Arkivering+i+fagarkivet#Arkiveringifagarkivet-Swagger-UIAPIklient). */
@Suppress("SpellCheckingInspection")
data class Journalpost(
    /** Dato forsendelsen ble mottatt fra avsender. Dersom datoMottatt er tom, settes verdien til dagens dato. Feltet kan kun settes for inngående journalposter. */
    val datoMottatt: Date,

    /** Unik id for forsendelsen som kan brukes til sporing gjennom verdikjeden. Eksempler på eksternReferanseId kan være en GUID, sykmeldingsId for sykmeldinger, Altinn ArchiveReference for Altinn-skjema eller SEDid for SED.
     * NB: Det er duplikatkontroll på eksternReferanseId. Dersom man sender inn en eksternReferanseId som allerede finnes i arkivet, vil tjenesten kaste feil (409 Conflict). */
    val eksternReferanseId: UUID,

    /** Tittel som beskriver forsendelsen samlet, feks "Søknad om dagpenger ved permittering". */
    val tittel: String,

    /** Type journalpost. */
    val journalposttype: Type,

    /** Temaet som forsendelsen tilhører, for eksempel “DAG” (Dagpenger). Påkrevd dersom Sak oppgis. */
    val tema: String,

    /** Behandlingstema for forsendelsen, for eksempel ab0001 (Ordinære dagpenger). Lovlige verdier finnes i i Felles Kodeverksløsning. */
    val behandlingstema: String,

    /** Kanalen som ble brukt ved innsending eller distribusjon. F.eks. NAV_NO, ALTINN eller EESSI. */
    val kanal: String,

    /** NAV-enheten som har journalført, eventuelt skal journalføre, forsendelsen. Ved automatisk journalføring uten mennesker involvert skal enhet settes til "9999". Konsument må sette journalfoerendeEnhet dersom tjenesten skal ferdigstille journalføringen. */
    val journalfoerendeEnhet: String,

    /** Identifikatoren til avsender/mottaker. Dette er normalt et fødselsnummer eller organisasjonsnummer, men valideres ikke. Dersom det ønskes å nullstille denne verdien, kan den settes til en tom string. */
    val avsenderMottaker: Ident,

    /** Brukeren (Patient i FHIR) som saken/dokumentet omhandler. */
    val bruker: Bruker,

    /** Referanse til sak i relevant fagssystem. */
    val sak: Sak,

    /** Første dokument blir tilknyttet som hoveddokument på journalposten. Øvrige dokumenter tilknyttes som vedlegg. Rekkefølgen på vedlegg beholdes ikke ved uthenting av journalpost. */
    val dokumenter: List<Dokument>,
) {
    enum class Type { INNGAAENDE, UTGAAENDE, NOTAT }

    data class Ident(
        val id: String,
        val idType: Type,
        val navn: String,
    ) {
        enum class Type { FNR, ORGNR, HPRNR, UTL_ORG }
    }

    data class Bruker(
        val id: String,
        val idType: Type,
    ) {
        enum class Type { FNR, ORGNR, AKTOERID }
    }

    data class Sak(
        val sakstype: Type,
        val fagsaksystem: System?,
        val fagsakId: String?
    ) {
        enum class Type {
            /** FAGSAK vil si at dokumentene tilhører en sak i et fagsystem. Dersom FAGSAK velges, må fagsakid og fagsaksystem oppgis. */
            FAGSAK,
            /** GENERELL_SAK kan brukes for dokumenter som skal journalføres, men som ikke tilhører en konkret fagsak. Generell sak kan ses på som brukerens “mappe” på et gitt tema. */
            GENERELL_SAK,
            /** ARKIVSAK skal kun brukes etter avtale. */
            ARKIVSAK
        }
        enum class System { FS38, FS36, UFM, OEBS, OB36, AO01, AO11, IT01, PP01, K9, BISYS, BA, EF, KONT, SUPSTONAD, OMSORGSPENGER, HJELPEMIDLER }
    }

    data class Dokument(
        /** Synlig i brukers journal på nav.no, samt i Gosys. */
        val tittel: String,
        /** Brevkoden sier noe om dokumentets innhold og oppbygning. For inngående dokumenter kan brevkoden være en NAV-skjemaID f.eks. “NAV 04-01.04” eller en SED-id. Brevkode skal ikke settes for ustrukturert, uklassifisert dokumentasjon, f.eks. brukeropplastede vedlegg. */
        val brevkode: String,
        val dokumentvarianter: List<Variant>,
    ) {
        data class Variant(
            /* Filtypen til filen som følger, feks PDFA, JSON eller XML. */
            val filtype: String,
            /** Base64 encoded data. */
            val fysiskDokument: String,
            /** ARKIV brukes for dokumentvarianter i menneskelesbart format (for eksempel PDFA). Gosys og nav.no henter arkivvariant og viser denne til bruker.
             * ORIGINAL skal brukes for dokumentvariant i maskinlesbart format (for eksempel XML og JSON) som brukes for automatisk saksbehandling
             * Alle dokumenter må ha én variant med variantFormat ARKIV. */
            val variantFormat: String,
        )
    }
}

data class Received(
    val info: Info,
    val date: Date,
    val files: Files,
)

data class Delivered(
    val info: Info,
    val hprNr: Int,
    val files: Files,
)

data class Info(
    val id: UUID,
    val title: String,
    val topic: String,
    val treatmentTopic: String,
    val subjectId: String,
)

class Files(
    val pdf: ByteArray,
    val original: ByteArray,
)
