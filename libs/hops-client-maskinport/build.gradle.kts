import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
    }
}

dependencies {
    api("com.nimbusds:nimbus-jose-jwt:9.13")
    api("io.ktor:ktor-client-core:1.6.3")
    api("io.ktor:ktor-client-serialization:1.6.3")
    api("io.ktor:ktor-serialization:1.6.3")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
