# EventStore
The purpose of this service is to be the master-store for FHIR messages. It exposes a simple RESTful API, decoupled from the underlying data-store.

The intent is that stored FHIR messages should be immutable and stored for a very long time, possibly surviving the popularity of current technologies: e.g. kafka and postgres. The API is therefore intentionally made as simple and minimal as possible.

Business requirements can be met by importing the messages into new services with appropriate technology, models and/or APIs: e.g. kafka, mongodb, hapi, sql, mq, etc. This pattern is inspired by [Projections in EventSourcing](https://domaincentric.net/blog/event-sourcing-projections).

This service's API should not be exposed to external clients, but is still made according to the [FHIR spec.](https://www.hl7.org/fhir/messaging.html). This is not required, but to be consistent.