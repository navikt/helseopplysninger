import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.30"
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showCauses = true
            showExceptions = true
            events("passed", "failed")
        }
    }
}

dependencies {
    implementation(project(":libs:hops-common-fhir"))
    implementation(project(":libs:hops-common-ktor"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:5.5.1")
    implementation("io.ktor:ktor-auth:1.6.3")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.3")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("io.ktor:ktor-webjars:1.6.3")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.3")
    implementation("no.nav.security:token-validation-ktor:1.3.8")
    implementation("org.flywaydb:flyway-core:7.14.1")
    implementation("org.jetbrains.exposed:exposed-java-time:0.34.1")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.34.1")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:5.5.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.5")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.postgresql:postgresql:42.2.23")
    runtimeOnly("org.webjars:swagger-ui:3.51.2")
    testImplementation(project(":libs:hops-common-test"))
    testRuntimeOnly("com.h2database:h2:1.4.200")
    testImplementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.2.2")
    testImplementation("io.ktor:ktor-serialization:1.6.3")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
