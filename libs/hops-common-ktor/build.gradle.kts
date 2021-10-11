import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showCauses = true
            showExceptions = true
            events("passed", "failed")
        }
    }
}

dependencies {
    api(project(":libs:hops-common-core"))
    api("io.ktor:ktor-client-auth:1.6.4")
    api("io.ktor:ktor-auth:1.6.4")
    api("io.ktor:ktor-server-core:1.6.4")
    implementation("no.nav.security:token-validation-ktor:1.3.8")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.5.1")
    implementation("com.sksamuel.hoplite:hoplite-hocon:1.4.9")
    implementation("com.sksamuel.hoplite:hoplite-yaml:1.4.7")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
