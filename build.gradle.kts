plugins {
    kotlin("jvm") version "1.6.0" apply false
    id("com.github.johnrengelman.shadow") version "7.1.0" apply false
    id("org.jlleitschuh.gradle.ktlint") version "10.2.0" apply false
    kotlin("plugin.serialization") version "1.6.0" apply false
}

subprojects {
    repositories {
        maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
        mavenCentral()
    }

    /**
     * Find dependency for configuration
     * ./gradlew findDep --configuration implementation --dependency rocksdbjni
     */
    task("findDep", type = DependencyInsightReportTask::class) {}

    /**
     * List all dependencies
     * ./gradlew allDeps
     */
    task("allDeps", type = DependencyReportTask::class) {}
}
