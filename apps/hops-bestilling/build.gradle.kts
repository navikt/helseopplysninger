import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.5.2"
val koin_version = "3.0.1-beta-2"
val hapi_version = "5.2.1"
val token_validation_version = "1.3.3"
val junit_version = "5.7.1"
val mock_oauth_version = "0.3.1"
val logback_version = "1.2.3"
val logstash_version = "6.6"
val kafka_version = "2.7.0"
val hoplite_version = "1.4.0"

plugins {
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
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapi_version")
    implementation("com.sksamuel.hoplite:hoplite-core:$hoplite_version")
    implementation("io.insert-koin:koin-ktor:$koin_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("org.apache.kafka:kafka-clients:$kafka_version")
    testImplementation("io.insert-koin:koin-test:$koin_version") { exclude(group = "junit", module = "junit") }
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapi_version")
    runtimeOnly("ch.qos.logback:logback-classic:$logback_version")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstash_version")
}
