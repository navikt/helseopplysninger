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
    test {
        useJUnitPlatform()
    }
}

dependencies {
    val junitVersion = "5.7.2"
    val ktorVersion = "1.6.0"
    val naviktTokenSupportVersion = "1.3.7"

    api("org.jetbrains.kotlin:kotlin-reflect:${kotlin.coreLibrariesVersion}") { because("Prevent different versions in classpath.") }
    implementation("com.fasterxml.uuid:java-uuid-generator:4.0.1")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-java:$ktorVersion")
    implementation("no.nav.security:token-client-core:$naviktTokenSupportVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.0")
    testImplementation("io.kotest:kotest-assertions-shared:4.6.0")
    testImplementation("io.ktor:ktor-jackson:$ktorVersion")
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude("org.jetbrains.kotlin", "kotlin-test-junit") }
    testImplementation("no.nav.security:mock-oauth2-server:0.3.3")
    testImplementation("no.nav.security:token-validation-ktor:$naviktTokenSupportVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
