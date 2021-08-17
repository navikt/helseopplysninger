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
    api(project(":libs:hops-common-ktor"))
    api("io.ktor:ktor-client-mock:1.6.2")
    api("no.nav.security:mock-oauth2-server:0.3.4")
    api("io.kotest:kotest-runner-junit5:4.6.1")
    api("io.ktor:ktor-server-test-host:1.6.2")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
