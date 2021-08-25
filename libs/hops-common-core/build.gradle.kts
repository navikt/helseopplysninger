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
    implementation("io.ktor:ktor-client-jackson:1.6.2")
    implementation("io.ktor:ktor-client-java:1.6.2")
    implementation("no.nav.security:token-client-core:1.3.8")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.5.1")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["test"].resources.srcDir("test/.config")
