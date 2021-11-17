# Versioning

2021-11-17 - proposed

## Context
FHIR supports multiple levels of versioning and is continuously developed, so new versions are expected to be released during the lifetime of *Helseopplysninger*.

In context of this ADR we refer to 2 types of versioning.
- The [FHIR specification version](https://www.hl7.org/fhir/versions.html#versions), notably **publication** and **major**, e.g. `4.2`. This version is important because e.g. the correct deserializer will have to be selected given the version.
- The **Profile version**, specifically [the Business version of a StructureDefinition](https://www.hl7.org/fhir/profiling.html), this is comparable to versioning a data model. This version is tied to the domain and allows for different Domain Event schemas (FHIR Message profiles) to be developed and versioned independently from each other.

### FHIR specification version
Messages transmitted through the *Helseopplysninger* platform shall be immutable and stored for a long time. Both the format and version of the Message shall therefore be stored together with the message using the [Mime Type Parameter format](https://www.hl7.org/fhir/versioning.html#mt-version), e.g. `application/fhir+json; fhirVersion=4.0`. We therefore also have to design our APIs so that they can support both the current and future versions of the FHIR specification.

One strategy of doing this for a RESTful API is to have the version as part of the URL, e.g. `[base]/4.0/Bundle` and `[base]/4.2/Bundle`. There are some disadvantages with this approach that makes it needlessly complex:

* Some endpoints that handles normative FHIR resources (e.g. CodeSystem) will not differ or shall at least be backward compatible. There is therefore no need to explicitly specify the version.
* A client might know the id of a resource, but not the FHIR version. It should still be able to request the resource at `[base]/{resourceType}/{id}` and receive the resource instance, and a `Content-Type` header specifying the version.

Another strategy is for the client to provide an `Accept` header where appropriate. This way all endpoints can follow the same URL pattern, even though they have different requirements regarding versioning.

## Decision
### FHIR Specification version
All transfers of FHIR resources shall contain the [Mime Type Parameter](https://www.hl7.org/fhir/http.html#version-parameter) with both format and version. In RESTful APIs this means using the `Content-Type` HTTP header on both requests and responses with a Body. On Kafka all records shall contain a [record header](https://kafka.apache.org/20/javadoc/index.html?org/apache/kafka/connect/header/Header.html) with `Content-Type` as key.

Every client request that may result in version ambiguity must specify the fhirVersion using the `Accept` header. The server shall **not** default to the latest supported version, because this will make adding new versions a breaking change.
Where editing the request headers is not possible we may consider specifying the version using the [_format](https://www.hl7.org/fhir/http.html#parameters) query parameter, e.g. `[base]/Bundle?_format=json;fhirVersion=4.0`, this is not part of the FHIR specification.

### Profile version
All FHIR messages shall populate the `Bundle.Meta.profile` with the canonical + version of the StructureDefinition it conforms to. A versioned profile for a FHIR message is akin to having a versioned **event schema** in EventSourcing. Versioning of profiles shall adhere to [Semantic versioning](https://semver.org/). 

The format of the canonical shall be according to the [specification](https://www.hl7.org/fhir/references.html#canonical), example: `http://fhir.nav.no/StructureDefinition/my-test-event|1.2.1`. This is the preferred solution, [see zullip discussion](https://chat.fhir.org/#narrow/stream/179263-fhir-messages/topic/.E2.9C.94.20Versioned.20Messages).

FHIR Messages in the EventStore shall be immutable and schema changes shall **not** trigger migrations\mappings to new versions. If historical messages must be mapped to a new format\schema this shall be done using an [upcaster](https://docs.axoniq.io/reference-guide/axon-framework/events/event-versioning) before the upcasted message is made available on e.g. a Kafka topic.
