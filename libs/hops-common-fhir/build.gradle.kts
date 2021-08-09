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
    test {
        useJUnitPlatform()
    }
}

dependencies {
    val hapiVersion = "5.4.2"
    val junitVersion = "5.7.2"

    api("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    api(project(":libs:hops-common-core"))
    implementation("com.google.auth:google-auth-library-oauth2-http:1.0.0")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    testImplementation("io.mockk:mockk:1.12.0")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["test"].resources.srcDir("test/.config")
