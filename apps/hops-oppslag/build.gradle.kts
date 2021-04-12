import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

object Version {
    const val ktor = "1.5.2"
    const val koin = "2.2.2"
    const val hapi = "5.2.1"
    const val token_validation = "1.3.3"
    const val junit = "5.7.1"
    const val mock_oauth = "0.3.1"
    const val logback = "1.2.3"
    const val logstash = "6.6"
    const val micrometer_prometheus = "1.6.5"
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
    // mainClass.set("io.ktor.server.netty.EngineMain") funker ikke med shadowJar atm
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
    implementation("io.ktor:ktor-client-jackson:${Version.ktor}")
    implementation("io.ktor:ktor-server-netty:${Version.ktor}")
    implementation("no.nav.security:token-validation-ktor:${Version.token_validation}")
    implementation("org.koin:koin-ktor:${Version.koin}")
    implementation("io.micrometer:micrometer-registry-prometheus:${Version.micrometer_prometheus}")
    implementation("io.ktor:ktor-metrics-micrometer:${Version.ktor}")
    testImplementation("io.ktor:ktor-server-test-host:${Version.ktor}") { exclude(group = "junit", module = "junit") }
    testImplementation("no.nav.security:mock-oauth2-server:${Version.mock_oauth}")
    testImplementation("org.junit.jupiter:junit-jupiter:${Version.junit}")
    testImplementation("org.koin:koin-test:${Version.koin}") { exclude(group = "junit", module = "junit") }
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:${Version.hapi}")
    runtimeOnly("ch.qos.logback:logback-classic:${Version.logback}")
    runtimeOnly("io.ktor:ktor-client-cio:${Version.ktor}")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${Version.logstash}")
}
