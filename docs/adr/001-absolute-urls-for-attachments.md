# Use of absolute URLs in attachments

* 2021-08-05 - proposed

# Context
En FHIR melding kan inneholde vedlegg i form av binærdata, dette kan enten være embedded som base64 eller en URL som peker til hvor det kan lastes ned. Se [Attachment](https://www.hl7.org/fhir/datatypes.html#Attachment) i FHIR spek.

## Decision
Ikke tillat å embedde binærdata i json, alle attachements må først lastes opp til en immutable [CDN](https://en.wikipedia.org/wiki/Content_delivery_network) (som vi eier) som det deretter linkes til. For å gjøre ting enkelt skal denne URLen være lik for alle aktører; dvs. både for EPJer og NAV-apper, både på internett og helsenett. 

## Consequences
For små binærfiler kan det være fristende å embedde dem i FHIR message json, men dette fungere dårlig for store binærfiler. 
Vi kan velge å tilby begge måter å gjøre det på og la klientene velge selv, men da må vi forholde oss til 2 flyter som igjen fører til mer logikk\kompleksitet og test-scenarioer, det er også gjerne enklere for klienter å slippe å ta valg. Store FHIR meldinger pga. embedded binær data kan også resultere i problemer med kafka, persistering, json-parsing, in-memory prosessering, caching, duplikat-kontroll, listing av meldinger etc.

Ved å alltid kreve at klienter laster opp vedlegg til en CDN kan vi samle all logikk relatert til dette i et API skreddersydd for dette formålet, dette muliggjør også funksjonalitet som ville vært vanskelig dersom vedlegg embeddes i FHIR. Denne tjenesten kan dermed håndtere:
* Virus scanning
* Duplikat-kontroll vha. hash.
* [Range-requests](https://developer.mozilla.org/en-US/docs/Web/HTTP/Range_requests) for opplasting\nedlasting av store filer.
* Tilgangsstyring.
* Støtte for henting av metadata om selve filen vha. HEAD etc.

Når det brukes hele URLer, f.eks `https://nav.no/helse/cdn/report.pdf` kan disse navigeres til direkte av klienter. Det krever derimot at disse URLene er veldig stabile og URLer som genereres i dag må fremdeles peke til samme fil mange år frem i tid. For å få til dette kan det bli vanskelig å bruke [NAV sin API Portal](https://github.com/navikt/nav-api-portal) for CDN tjenesten fordi eksterne (EPJer) og interne (NAV) apper da må forholde seg til forskjellig domene\host.
Det har vært vurdert å introdusere logikk som transformerer vedlegg-URLene før de presenteres til EPJer eller NAV apper slik at FHIR meldingene ser forskjellig ut. Dette er derimot en uønsket kompleksitet, det vil også skape forvirring når URLene brukes av mennesker f.eks i support saker.
En vanlig måte å representere en referanse til en ressurs uavhengig av domene/host er å bruke en identifikator som referanse istedenfor en hel URL.
Dette alternativet bryter med FHIR spek fordi et vedlegg i FHIR krever en full URL, en FHIR melding skal også inneholde hele konteksten og kan ikke være implsitt om hvor/hvordan en fil hentes.
I tillegg er dette en løsning som løser problemet ved å flytte kompleksiteten til klientene ved å bare gi dem en ID istedenfor en brukbar URL, dette bør vi spare dem for.
