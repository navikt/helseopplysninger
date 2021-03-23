import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Version {
    const val ktor = "1.5.1"
    const val koin = "2.2.2"
    const val hapi = "5.2.1"
    const val token_validation = "1.3.3"
    const val junit = "5.7.1"
    const val mock_oauth = "0.3.1"
    const val logback = "1.2.3"
    const val logstash = "6.6"
    const val kafka = "2.7.0"
}

plugins {
    java
    application
    kotlin("jvm") version "1.4.21"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

application {
    mainClass.set("no.nav.helse.hops.ApplicationKt")
}

repositories {
    mavenCentral()
    jcenter()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
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
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:${Version.hapi}")
    implementation("org.apache.kafka:kafka-clients:${Version.kafka}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Version.junit}")
    testImplementation("org.koin:koin-test:${Version.koin}") { exclude(group = "junit", module = "junit") }
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:${Version.hapi}")
    runtimeOnly("ch.qos.logback:logback-classic:${Version.logback}")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${Version.logstash}")
}
