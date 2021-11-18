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
    api(kotlin("test"))
    api("no.nav.security:mock-oauth2-server:0.4.0")
    api("io.ktor:ktor-client-mock:1.6.5") // Ktor - http mock
    api("io.ktor:ktor-server-test-host:1.6.5") { // Ktor - test engine
        // Transitive dependency net.java.dev.jna:jna is GPL 1.0 licenced
        // Transitive dependency net.java.dev.jna:jna-platform is GPL 1.0 licenced
        exclude("org.jetbrains.kotlinx", "kotlinx-coroutines-debug")
    }
    api("io.ktor:ktor-jackson:1.6.5")
    api("io.ktor:ktor-server-netty:1.6.5")
    api("no.nav.security:token-validation-ktor:1.3.9")
    api("io.mockk:mockk:1.12.1")
    api("uk.org.webcompere:system-stubs-jupiter:1.2.0")
    api("org.junit.jupiter:junit-jupiter:5.8.1")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
