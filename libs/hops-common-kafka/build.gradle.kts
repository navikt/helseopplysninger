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
    api("org.apache.kafka:kafka-clients:2.8.1")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
