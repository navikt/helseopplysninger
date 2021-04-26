package no.nav.helse.hops.infrastructure

import ca.uhn.fhir.context.FhirContext
import ca.uhn.fhir.context.FhirVersionEnum
import ca.uhn.fhir.rest.client.api.IGenericClient
import ca.uhn.fhir.rest.client.api.ServerValidationModeEnum
import com.sksamuel.hoplite.ConfigLoader
import no.nav.helse.hops.domain.BestillingConsumerJob
import no.nav.helse.hops.domain.BestillingProducerJob
import no.nav.helse.hops.domain.FhirRepository
import no.nav.helse.hops.domain.FhirRepositoryImpl
import no.nav.helse.hops.domain.FhirResourceValidator
import no.nav.helse.hops.domain.MessageBus
import no.nav.helse.hops.koin.HttpRequestKoinScope
import no.nav.helse.hops.koin.scopedClosable
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

        single<FhirResourceValidator> { FhirResourceValidatorHapi }
        single<MessageBus> { MessageBusKafka(get(), get(), get()) }
        single { createHapiFhirClient(get()) }
        single<FhirRepository> { FhirRepositoryImpl(get(), getLogger<FhirRepositoryImpl>()) }

        singleClosable { KafkaFactory.createFhirProducer(get()) }
        singleClosable { KafkaFactory.createFhirConsumer(get()) }
        singleClosable(createdAtStart = true) {
            BestillingConsumerJob(get(), getLogger<BestillingConsumerJob>(), get(), get(), get())
        }
    }

    val scopeModule = module {
        scope<HttpRequestKoinScope> {
            scopedClosable { BestillingProducerJob(get(), get(), get()) }
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
    return factory.newGenericClient(config.baseUrl).apply { registerInterceptor(interceptor) }
}

private inline fun <reified T : Any> getLogger(): Logger = LoggerFactory.getLogger(T::class.java)
