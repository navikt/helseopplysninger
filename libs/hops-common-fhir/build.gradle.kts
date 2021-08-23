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
    api("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.4.2")
    implementation("com.google.auth:google-auth-library-oauth2-http:1.1.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:5.4.2")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["test"].resources.srcDir("test/.config")
