rootProject.name = "helseopplysninger"
include(":hops-oppslag")
include(":hops-api")
include(":hops-bestilling")
include(":hops-hapi-fhir-server")

project(":hops-oppslag").projectDir = file("apps/hops-oppslag")
project(":hops-bestilling").projectDir = file("apps/hops-bestilling")
project(":hops-api").projectDir = file("apps/hops-api")
project(":hops-hapi-fhir-server").projectDir = file("apps/hops-hapi-fhir-server")
