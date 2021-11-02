import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("e2e.AppKt")
}

dependencies {
    implementation(project(":libs:hops-client-maskinport"))
    implementation(project(":libs:hops-common-kafka"))
    implementation(project(":libs:hops-common-ktor"))
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.11")
    implementation("io.ktor:ktor-client-serialization:1.6.5")
    implementation("io.ktor:ktor-client-core:1.6.5")
    implementation("io.ktor:ktor-client-cio:1.6.5")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.5")
    implementation("io.ktor:ktor-serialization:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.5")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.5")
    implementation("org.apache.kafka:kafka-clients:2.8.1")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.6")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    testImplementation(project(":libs:hops-common-test"))
    testImplementation(project(":libs:hops-common-kafka-test"))
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
    }

    withType<Test> {
        useJUnitPlatform()
        testLogging {
            events("skipped", "failed")
            showExceptions = true
            exceptionFormat = TestExceptionFormat.FULL
        }

        jvmArgs = listOf("--add-opens=java.base/java.util=ALL-UNNAMED")
    }
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
