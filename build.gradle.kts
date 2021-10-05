plugins {
    kotlin("jvm") version "1.5.31" apply false
    id("com.github.johnrengelman.shadow") version "7.0.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0" apply false
    kotlin("plugin.serialization") version "1.5.31" apply false
}

subprojects {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        mavenCentral()
    }
}
