package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.api.EncodingEnum
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.domain.TaskChangeFeed
import no.nav.helse.hops.domain.TaskStateChangeSubscriberJob
import no.nav.helse.hops.koin.singleClosable
import no.nav.helse.hops.security.fhir.OauthRequestInterceptor
import no.nav.helse.hops.security.oauth.OAuth2ClientFactory
import org.koin.dsl.module
import org.slf4j.Logger
import org.slf4j.LoggerFactory

object KoinBootstrapper {
    val singleModule = module {
        data class ConfigRoot(
            val kafka: Configuration.Kafka,
            val fhirMessaging: Configuration.FhirMessaging,
            val fhirServer: Configuration.FhirServer
        )

        single { ConfigLoader().loadConfigOrThrow<ConfigRoot>("/application.conf") }
        single { get<ConfigRoot>().kafka }
        single { get<ConfigRoot>().fhirMessaging }
        single { get<ConfigRoot>().fhirServer }

        single { createHapiFhirClient(get()) }
        single<TaskChangeFeed> { FhirHistoryFeedHapi(get()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) {
            TaskStateChangeSubscriberJob(get(), getLogger<TaskStateChangeSubscriberJob>())
        }
    }
}

private fun createHapiFhirClient(config: Configuration.FhirServer): IGenericClient {
    val oauthClient = OAuth2ClientFactory.create(
        config.discoveryUrl, config.clientId, config.clientSecret
    )

    val interceptor = OauthRequestInterceptor(oauthClient, config.scope)

    // So that we dont start by requesting /metadata.
    val ctx = FhirContext.forCached(FhirVersionEnum.R4)
    val factory = ctx.restfulClientFactory.apply { serverValidationMode = ServerValidationModeEnum.NEVER }
    return factory.newGenericClient(config.baseUrl).apply {
        registerInterceptor(interceptor)
        encoding = EncodingEnum.JSON
    }
}

private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
