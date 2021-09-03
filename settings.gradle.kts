rootProject.name = "helseopplysninger"

include("apps:autotest")
include("apps:hops-api")
include("apps:hops-eventreplaykafka")
include("apps:hops-eventsinkkafka")
include("apps:hops-eventstore")
include("apps:hops-fileshare")
include("apps:hops-test-external")
include("apps:hops-test-e2e")

include("libs:hops-common-core")
include("libs:hops-common-fhir")
include("libs:hops-common-ktor")
include("libs:hops-common-test")
