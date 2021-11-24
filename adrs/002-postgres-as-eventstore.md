# PostgreSQL as event (FHIR message) storage

* 2021-11-09 - accepted

# Context
Master dataen i helseopplysninger-kontekst er meldingene som sendes mellom NAV og helsesektoren. Denne dataen ønsker vi å lagre rått og uendret til "evig tid". Ingen modifiseringer eller skjema-migreringer skal gjøres. For å dekke forskjellige behov vil det heller bli lagt opp til å lage [projections](https://zimarev.com/blog/event-sourcing/projections/).

Disse meldingene ønsker vi å lagre på en enkel måte med fokus på kontroll, sikkerhet, redundans og immutability. Det må også være enkelt å flytte dataen til en annen type teknologi dersom nye behov oppstår. Databasen må støtte unik-garanti på id, men trenger ingen transaksjonsstøtte utover dette. Ytelse er heller ikke prioritert siden skriving og lesing gjøres på Kafka og spesialiserte projection-databaser.

Selv om tjenesten heter EventStore trenger den ikke en omfattende SAAS tjeneste slik som f.eks [EventStoreDB](https://www.eventstore.com/). Vår EventStore skal tilgjengeliggjøres gjennom et enkelt REST-api som (med hensikt) stiller få krav til underliggende databaseteknologi. 

## Decision
PostgreSQL allerede er en etablert teknologi i NAV som mange har kjennskap til og NAIS har gode rutiner for. Den dekker alle våre behov og mer til. Den er også enkel å teste, både lokalt vha. docker-compose og i kode.

PostgreSQL er også støttet i alle de store skyleverandørene og kan enkelt kjøres on-prem. Selv om dette neppe er relevant for NAV kan det være det for andre mtp. at dette er et open-source prosjekt.

Følgende alternativer ble vurdert:
- Kafka (som master for data)
- Google Cloud Storage (hver melding som egen fil)
- Generic FHIR store (HAPI, Google Healthcare API)
- EventStoreDB
- MongoDB