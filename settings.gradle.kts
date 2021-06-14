rootProject.name = "helseopplysninger"

include("apps:autotest")
include("apps:hops-api")
include("apps:hops-bestilling")
include("apps:hops-oppslag")
include("apps:hops-hapi-fhir-server")
include("apps:hops-hendelser")
include("apps:hops-EventStore")
include("apps:hops-EventSinkKafka")

include("libs:hops-common-core")
include("libs:hops-common-fhir")
include("libs:hops-common-ktor")
