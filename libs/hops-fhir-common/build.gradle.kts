import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hapi_version = "5.2.1"
val kotlinx_version = "1.4.3"

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

repositories {
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    mavenCentral()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        dependsOn("ktlintFormat")
    }
    test {
        useJUnitPlatform()
    }
}

dependencies {
    api(project(":libs:hops-client-security"))
    api("ca.uhn.hapi.fhir:hapi-fhir-base:$hapi_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinx_version")
}