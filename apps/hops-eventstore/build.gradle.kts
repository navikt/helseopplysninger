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
    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:5.5.3")
    implementation("io.ktor:ktor-auth:1.6.5")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.5")
    implementation("io.ktor:ktor-server-netty:1.6.5")
    implementation("io.ktor:ktor-webjars:1.6.5")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.0")
    implementation("no.nav.security:token-validation-ktor:1.3.9")
    implementation("org.flywaydb:flyway-core:8.0.4")
    implementation("org.jetbrains.exposed:exposed-java-time:0.36.2")
    implementation("org.jetbrains.exposed:exposed-jdbc:0.36.2")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:5.6.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.7")
    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.postgresql:postgresql:42.3.1")
    runtimeOnly("org.webjars:swagger-ui:4.1.0")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
