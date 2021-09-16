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
    api("com.nimbusds:nimbus-jose-jwt:9.13")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
