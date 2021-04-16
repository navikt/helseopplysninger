rootProject.name = "helseopplysninger"

include("apps:hops-api")
include("apps:hops-bestilling")
include("apps:hops-oppslag")
include("apps:hops-hapi-fhir-server")

include("libs:hops-common-core")
include("libs:hops-common-fhir")
include("libs:hops-common-ktor")