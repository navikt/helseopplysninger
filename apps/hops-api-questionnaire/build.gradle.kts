import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("questionnaire.AppKt") // Required by shadowJar
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showExceptions = true
            exceptionFormat = TestExceptionFormat.FULL
        }
    }
}

dependencies {
    implementation(project(":libs:hops-common-fhir"))
    implementation(project(":libs:hops-common-ktor"))
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("io.ktor:ktor-client-serialization:1.6.5")
    implementation("io.ktor:ktor-client-jackson:1.6.5")
    implementation("io.ktor:ktor-jackson:1.6.5")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.5")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("io.ktor:ktor-webjars:1.6.3")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.7")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.webjars:swagger-ui:4.1.0")
    testImplementation(project(":libs:hops-common-test"))
    testImplementation("com.sksamuel.hoplite:hoplite-yaml:1.4.15")
    testImplementation("io.kotest:kotest-assertions-core:4.6.3")
    testImplementation("io.kotest:kotest-assertions-ktor:4.4.3")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
sourceSets["test"].resources.srcDir("test/resources")
