# API Questionnaire
The responsibility of this service is to pull [FHIR Questionnaire](https://www.hl7.org/fhir/questionnaire.html) resources from the [navikt/fhir-questionnaires](https://github.com/navikt/fhir-questionnaires) GitHub repository and make them available through a minimal (but valid) read-only [FHIR RESTful API](https://www.hl7.org/fhir/http.html). This API can be used by FHIR compatible services, such as validators and front-end form renderers.

Questionnaires are manually developed and maintained in the GitHub repository and published as releases. By using [GitHub events](https://docs.github.com/en/developers/webhooks-and-events/webhooks) the service will be notified about new releases and can reactively pull resources.

The number of unique Questionnaires (including history) is not expected to be large. The resources are therefore stored in an in-memory list which is built on server startup.

## Relevant FHIR links
### Data types
https://www.hl7.org/fhir/datatypes.html

### API
https://www.hl7.org/fhir/http.html

#### Read
https://www.hl7.org/fhir/http.html#read

#### Search
https://www.hl7.org/fhir/search.html

### Resources
https://www.hl7.org/fhir/resourcelist.html

#### Questionnaire
- http://hl7.org/fhir/questionnaire-example.json.html
- https://www.hl7.org/fhir/questionnaire.html
- https://www.hl7.org/fhir/questionnaire.html#search
