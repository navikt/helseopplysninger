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
    api(project(":libs:hops-common-core"))
    api("io.insert-koin:koin-ktor:3.0.1")
    implementation("com.sksamuel.hoplite:hoplite-hocon:1.4.0")
}
