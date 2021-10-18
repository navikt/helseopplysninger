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
    withType<Test> {
        useJUnitPlatform()
        testLogging {
            showCauses = true
            showExceptions = true
            events("passed", "failed")
        }
    }
}

dependencies {
    api(kotlin("reflect"))
    implementation("io.ktor:ktor-client-jackson:1.6.4")
    implementation("io.ktor:ktor-client-java:1.6.4")
    implementation("no.nav.security:token-client-core:1.3.9")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.2")
    testImplementation(project(":libs:hops-common-test"))
    testImplementation("io.kotest:kotest-property:4.6.3") // Kotest - property testing
    testImplementation("io.kotest:kotest-assertions-ktor:4.4.3") // Kotest - ktor matchers
    testImplementation("io.kotest:kotest-assertions-shared:4.6.3")
    testRuntimeOnly("io.kotest:kotest-runner-junit5:4.6.3") // Kotest - test framework
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["test"].resources.srcDir("test/.config")
