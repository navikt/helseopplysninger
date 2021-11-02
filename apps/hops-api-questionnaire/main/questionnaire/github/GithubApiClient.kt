package questionnaire.github

import com.fasterxml.jackson.databind.DeserializationFeature
import io.ktor.client.HttpClient
import io.ktor.client.call.receive
import io.ktor.client.features.json.JacksonSerializer
import io.ktor.client.features.json.JsonFeature
import io.ktor.client.request.get
import io.ktor.client.statement.HttpStatement
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.jvm.javaio.toInputStream
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import mu.KotlinLogging
import org.hl7.fhir.r4.model.Questionnaire
import questionnaire.Config
import questionnaire.fhir.FhirResourceFactory
import questionnaire.fhir.QuestionnaireEnricher
import questionnaire.store.QuestionnaireStore
import java.io.BufferedReader

private val log = KotlinLogging.logger {}

class GithubApiClient(private val config: Config.Github.Api) {
    private val downloadClient = HttpClient()
    private val jsonClient = HttpClient {
        install(JsonFeature) {
            serializer = JacksonSerializer {
                // We will only grab the relevant fields from the GitHub Api JSON responses
                configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            }
        }
    }

    init {
        runBlocking {
            launch {
                fetchQuestionnaires(jsonClient.get("${config.url}/repos/${config.repo}/releases"))
                    .collect(QuestionnaireStore::add)
            }
        }
    }

    suspend fun fetchAndStoreLatest() {
        val release = jsonClient.get<Release>("${config.url}/repos/${config.repo}/releases/latest")
        log.info { "Triggered from github event" }
        log.info { release }
        fetchQuestionnaires(listOf(release))
            .collect(QuestionnaireStore::add)
    }

    private fun fetchQuestionnaires(releases: List<Release>): Flow<Questionnaire> = flow {
        releases.forEach { release ->
            release.assets
                .map(Asset::browser_download_url)
                .map { download(it) }
                .map(FhirResourceFactory::questionnaire)
                .map { QuestionnaireEnricher.enrich(release.created_at, it) }
                .forEach { emit(it) }
        }
    }

    private suspend fun download(downloadUrl: String): String =
        downloadClient.get<HttpStatement>(downloadUrl).execute { response ->
            val channel: ByteReadChannel = response.receive()
            withContext(Dispatchers.IO) {
                channel.toInputStream()
                    .bufferedReader()
                    .use(BufferedReader::readText)
            }
        }
}
