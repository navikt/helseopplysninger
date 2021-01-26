rootProject.name = "helseopplysninger"
include(":hops-oppslag")
include(":hops-api")

project(":hops-oppslag").projectDir = file("apps/hops-oppslag")
project(":hops-api").projectDir = file("apps/hops-api")