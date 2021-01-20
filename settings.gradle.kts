rootProject.name = "helseopplysninger"
include(":hops-oppslag")
include(":hops-api")

project(":hops-oppslag").projectDir = file("apps/hops-oppslag")
project(":hops-api").projectDir = file("apps/hops-api")
project(":hops-hapi-starter").projectDir = file("apps/hops-hapi-starter")