import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
}

dependencies {
    implementation("no.nav:kafka-embedded-env:2.8.0") {
        exclude("io.confluent", "kafka-schema-registry") // not used (requires mock even if unused)
        exclude("org.apache.kafka", "kafka-streams") // not used (Contains rocksbdjni with GPL 2.0 licence)
    }
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
