import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("eventstore.AppKt")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
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
    implementation("io.ktor:ktor-auth:1.6.4")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.4")
    implementation("io.ktor:ktor-webjars:1.6.4")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.4")
    implementation("no.nav.security:token-validation-ktor:1.3.9")
    implementation("org.flywaydb:flyway-core:7.15.0")
    implementation("org.jetbrains.exposed:exposed-java-time:0.35.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.35.1")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:5.5.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.6")
    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.postgresql:postgresql:42.2.24")
    runtimeOnly("org.webjars:swagger-ui:3.52.3")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
