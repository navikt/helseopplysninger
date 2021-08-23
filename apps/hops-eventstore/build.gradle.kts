import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain") // Required by shadowJar
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
    val exposedVersion = "0.33.1"
    val hapiVersion = "5.5.0"
    val junitVersion = "5.7.2"
    val ktorVersion = "1.6.2"

    implementation("ca.uhn.hapi.fhir:hapi-fhir-validation:$hapiVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.ktor:ktor-webjars:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.3")
    implementation("no.nav.security:token-validation-ktor:1.3.8")
    implementation("org.flywaydb:flyway-core:7.14.0")
    implementation("org.jetbrains.exposed:exposed-java-time:$exposedVersion")
    implementation("org.jetbrains.exposed:exposed-jdbc:$exposedVersion")
    implementation(project(":libs:hops-common-fhir"))
    implementation(project(":libs:hops-common-ktor"))
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-validation-resources-r4:$hapiVersion")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.5")
    runtimeOnly("com.h2database:h2:1.4.200")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.postgresql:postgresql:42.2.23")
    runtimeOnly("org.webjars:swagger-ui:3.51.2")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude("org.jetbrains.kotlin", "kotlin-test-junit") }
    testImplementation("no.nav.security:mock-oauth2-server:0.3.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation("org.testcontainers:junit-jupiter:1.16.0")
    testImplementation(kotlin("test-junit5"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
