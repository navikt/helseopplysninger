# Dialogmelding
The responsibility of this service is to pull relevant FHIR messages from Kafka, generate a PDF and send it as a Dialogmelding of type DIALOG_NOTAT to the relevant receiver identified by HER-id.

The intent is to use a REST API to generate and return a PDF from a FHIR message json. This convert-api should be implemented by the front-end team according to [the convert operation spec.](http://hl7.org/fhir/resource-operation-convert.html), this is yet to be done.

The PDF is embedded in a XML, [see NAV's dialogmelding doc.](https://www.nav.no/no/nav-og-samfunn/samarbeid/leger-og-andre-behandlere/digital-sykemelding-informasjon-til-den-som-sykmelder/ny-sykmelding-og-dialogmeldinger/digital-sykmelding-og-dialogmeldinger-tekniske-spesifikasjoner), and sent to Emottak over IBM MQ. Emottak adds a XML Digital Signature and routes the Dialogmelding to the receiver using SMTP.