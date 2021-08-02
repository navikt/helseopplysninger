plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.20"
    id("org.jlleitschuh.gradle.ktlint")
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    val junitVersion = "5.7.2"
    val ktorVersion = "1.6.1"

    api(project(":libs:hops-common-fhir"))
    implementation("io.github.cdimascio:dotenv-kotlin:6.2.2")
    implementation("io.ktor:ktor-auth:1.6.1")
    implementation("no.nav.security:token-validation-ktor:1.3.8")
    implementation("org.apache.kafka:kafka-clients:2.8.0")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("io.ktor:ktor-server-netty:$ktorVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude("org.jetbrains.kotlin", "kotlin-test-junit") }
    testImplementation("no.nav.security:mock-oauth2-server:0.3.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
