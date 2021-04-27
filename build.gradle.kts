plugins {
    kotlin("jvm") version "1.4.32" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.0.0" apply false
}

subprojects {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        mavenCentral()
    }
}