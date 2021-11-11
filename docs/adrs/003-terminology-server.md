# Terminology Server


## Context
Within healthcare the use of code systems such as `ICD-10` or `SNOMED-CT` is prevalent. These code systems help communicate about diseases and other medical terms in a structured way. These code systems can also represent the relation between the codes it contains, which can result in a 

Healthcare systems make use of a terminology server that is able to provide the functionality needed to navigate these complex systems in a performant way. The FHIR spec defines how to represent and how to interact with these systems throught the [Terminology Service](https://www.hl7.org/fhir/terminology-service.html). To be able to provide this functionality in a performant way the implementation of this service needs to persist the resources ([CodeSystem](https://www.hl7.org/fhir/codesystem.html), [ValueSet](https://www.hl7.org/fhir/valueset.html), [ConceptMap](https://www.hl7.org/fhir/conceptmap.html)) with this in mind.

## Decision
The [HAPI-FHIR JPA Server](https://hapifhir.io/hapi-fhir/docs/server_jpa/introduction.html) provides an implementation of the Terminology Service that can provide the functionality in a performant way (https://hapifhir.io/hapi-fhir/docs/server_jpa/terminology.html). The persistence layer uses JPA so any SQL database can be used, to provide som of the search functionality the server can use either `Elasticsearch` or `Lucine` (local file system). 

In the context of this project we just make use of the `$expand` operation, so the default setup using `H2` and `Lucine` is enough for our use-case. However it wouldn't be acceptable to build the database after each deployment since our application un on `Kubernetes`, to overcome this we pre-build the database and index files and copy them to the container that will run on `Kubernetes`. This allows us to scale the number of instances and not having to support an `Elasticsearch` cluster.   

Other alternatives that were considered:
- [Google Healthcare API](https://cloud.google.com/healthcare-api/docs/concepts/fhir): Unfortunately there is no information about the way this server handles terminologies, and just refers to a generic FHIR stores for all resources, since this server is close-source it is not possible to assess this solution. A ticket was submitted to Google but we didn't receive any reply. The HAPI server also provides a CLI that simplifies the management of terminologies, this is not provided by this implementation. 
- Custom implementation: Since we use just a limited amount of the functionality of the terminology server, we could implement the server ourselves and only support the functionality we use. However this would require unnecessary development, and we might start using more features in the future.
- HAPI-FHIR JPA Server using `Postgres` and `Elasticsearch` as storage: This would allow to have a more durable persistence layer, however we currently don't use `Elasticsearch` and the support provided by the `NAIS` platform is [limited](https://doc.nais.io/persistence/elastic-search/#get-your-own), we also considered using `Postgres` with `Lucine` however this is [not supported](https://chat.fhir.org/#narrow/stream/179167-hapi/topic/JPA.20starter.20-.20Lucine.20.2B.20Postgres) by the server. 
