# HOPS Terminology

This service is responsible for providing the functionality described by the [Terminology Service](https://www.hl7.org/fhir/terminology-service.html) FHIR spec. It uses the implementation provided by the [HAPI-FHIR JAP Server](https://hapifhir.io/hapi-fhir/docs/server_jpa/introduction.html) which internally uses `H2` and `Lucine` as storage and index using local files to persist the data. 

This application will be deployed in a `Kubernetes` environment, so we have to expect that application might be re-scheduled in a different node at any point. To overcome this, we build the database and index files in the CI pipeline and copy the files to the final image. This is done by the `build.sh` script, that downloads the CodeSystem files and the HAPI CLI, it then starts an instance of the server and loads the CodeSystem into it. However there are many operations that happen in the background when loading a CodeSystem, so we have to poll the instance until we receive the expected codes when calling the `$expand` operation to ensure that the background processes are finished. Unfortunately this process can take some time (16m sometimes) as this is done by a scheduled background process, we tried to find a way to control this trigger but we [couldn't](https://chat.fhir.org/#narrow/stream/179167-hapi/topic/hapi-fhir-jpaserver-starter.20ValueSet.20.24expand) find a solution.

With this solution the resulting image becomes "stateless" so write operations should not be permitted as they will not be durable.

