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
    val ktorVersion = "1.6.0"
    val junitVersion = "5.7.2"
    val koinVersion = "3.0.2"

    api(project(":libs:hops-common-core"))
    api("io.insert-koin:koin-ktor:$koinVersion")
    api("io.insert-koin:koin-logger-slf4j:$koinVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:5.4.0")
    implementation("com.sksamuel.hoplite:hoplite-hocon:1.4.1")

    testImplementation(kotlin("test-junit5"))
    testImplementation("io.mockk:mockk:1.11.0")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude("org.jetbrains.kotlin", "kotlin-test-junit") }
    testImplementation("io.kotest:kotest-assertions-shared:4.6.0")
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
