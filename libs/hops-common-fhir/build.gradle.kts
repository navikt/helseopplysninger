import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
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
    api(project(":libs:hops-common-core"))
    api("ca.uhn.hapi.fhir:hapi-fhir-base:5.3.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.3")
}
