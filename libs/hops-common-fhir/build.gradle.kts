import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val hapiVersion = "5.2.1"
val kotlinxVersion = "1.4.3"

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
    api("ca.uhn.hapi.fhir:hapi-fhir-base:$hapiVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion")
}
