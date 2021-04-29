package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Bundle

interface FhirMessageBus : MessageBusConsumer<Bundle>, MessageBusProducer<Bundle>
