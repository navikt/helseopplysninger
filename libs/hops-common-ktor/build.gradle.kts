import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
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
    api("io.ktor:ktor-client-auth:1.6.5")
    api("io.ktor:ktor-auth:1.6.5")
    api("io.ktor:ktor-server-core:1.6.5")
    implementation("no.nav.security:token-validation-ktor:1.3.9")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.6.0")
    implementation("com.sksamuel.hoplite:hoplite-hocon:1.4.15")
    implementation("com.sksamuel.hoplite:hoplite-yaml:1.4.15")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
