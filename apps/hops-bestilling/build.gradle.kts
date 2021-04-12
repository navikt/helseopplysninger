import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Version {
    const val ktor = "1.5.2"
    const val koin = "3.0.1-beta-2"
    const val hapi = "5.2.1"
    const val token_validation = "1.3.3"
    const val junit = "5.7.1"
    const val mock_oauth = "0.3.1"
    const val logback = "1.2.3"
    const val logstash = "6.6"
    const val kafka = "2.7.0"
    const val hoplite = "1.4.0"
}

plugins {
    java
    application
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "6.1.0"
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
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
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:${Version.hapi}")
    implementation("com.sksamuel.hoplite:hoplite-core:${Version.hoplite}")
    implementation("io.insert-koin:koin-ktor:${Version.koin}")
    implementation("io.ktor:ktor-server-netty:${Version.ktor}")
    implementation("org.apache.kafka:kafka-clients:${Version.kafka}")
    testImplementation("io.insert-koin:koin-test:${Version.koin}") { exclude(group = "junit", module = "junit") }
    testImplementation("org.junit.jupiter:junit-jupiter:${Version.junit}")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:${Version.hapi}")
    runtimeOnly("ch.qos.logback:logback-classic:${Version.logback}")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${Version.logstash}")
}
