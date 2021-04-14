import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.5.2"
val navikt_token_support_version = "1.3.3"
val junit_version = "5.7.1"
val mock_oauth_version = "0.3.1"
val logback_version = "1.2.3"
val kotlinx_version = "1.4.3"
val kotest_version = "4.4.3"

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0"
}

repositories {
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    mavenCentral()
}

tasks {
    withType<KotlinCompile> {
        kotlinOptions.jvmTarget = "11"
        dependsOn("ktlintFormat")
    }
    test {
        useJUnitPlatform()
    }
}

dependencies {
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinx_version")
    implementation("no.nav.security:token-client-core:$navikt_token_support_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version") { exclude(group = "junit", module = "junit") }
    testImplementation("no.nav.security:mock-oauth2-server:$mock_oauth_version")
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotest_version")
    testImplementation("io.ktor:ktor-server-netty:$ktor_version")
    testImplementation("io.ktor:ktor-jackson:$ktor_version")
    testImplementation("no.nav.security:token-validation-ktor:$navikt_token_support_version")
    testRuntimeOnly("io.kotest:kotest-runner-junit5-jvm:$kotest_version")
    testRuntimeOnly("ch.qos.logback:logback-classic:$logback_version")
}