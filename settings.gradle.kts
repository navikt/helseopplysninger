rootProject.name = "helseopplysninger"

include("apps:autotest")
include("apps:hops-api")
include("apps:hops-eventreplaykafka")
include("apps:hops-eventsinkkafka")
include("apps:hops-eventstore")
include("apps:hops-oppslag")
include("apps:hops-test-external")

include("libs:hops-common-core")
include("libs:hops-common-fhir")
include("libs:hops-common-ktor")
