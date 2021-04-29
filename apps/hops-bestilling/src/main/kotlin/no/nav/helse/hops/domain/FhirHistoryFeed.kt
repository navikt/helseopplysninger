package no.nav.helse.hops.domain

import org.hl7.fhir.r4.model.Resource

interface FhirHistoryFeed : MessageBusConsumer<Resource>