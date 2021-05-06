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
    val hapiVersion = "5.3.3"

    api(project(":libs:hops-common-core"))
    api("ca.uhn.hapi.fhir:hapi-fhir-base:$hapiVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.3")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
}
