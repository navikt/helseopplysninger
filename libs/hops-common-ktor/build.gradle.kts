import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val koinVersion = "3.0.1-beta-2"

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
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
    api(project(":libs:hops-common-core"))
    api("io.insert-koin:koin-ktor:$koinVersion")
}
