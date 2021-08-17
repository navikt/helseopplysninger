plugins {
    kotlin("jvm") version "1.5.21" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0" apply false
    id("io.kotest") version "0.3.8" apply false
}

subprojects {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        mavenCentral()
    }
}
