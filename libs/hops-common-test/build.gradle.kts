import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
    }
}

dependencies {
    api(project(":libs:hops-common-ktor"))
    api("no.nav.security:mock-oauth2-server:0.3.4")
    api("io.ktor:ktor-client-mock:1.6.3") // Ktor - http mock
    api("io.ktor:ktor-server-test-host:1.6.3") // Ktor - test engine
    api("io.kotest:kotest-runner-junit5:4.6.2") // Kotest - test framework
    api("io.kotest:kotest-property:4.6.2") // Kotest - property testing
    api("io.kotest:kotest-assertions-ktor:4.4.3") // Kotest - ktor matchers
    api("io.kotest:kotest-assertions-shared:4.6.2")
    api("io.ktor:ktor-jackson:1.6.3")
    api("io.ktor:ktor-server-netty:1.6.3")
    api("no.nav.security:mock-oauth2-server:0.3.4")
    api("no.nav.security:token-validation-ktor:1.3.8")
    api("io.mockk:mockk:1.12.0")
    // todo : exclude junit/jupiter
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
