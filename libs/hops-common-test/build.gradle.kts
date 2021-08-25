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

    register<DependentsTask>("dependents")
}

dependencies {
    api(project(":libs:hops-common-ktor"))
    api("no.nav.security:mock-oauth2-server:0.3.4")
    api("io.ktor:ktor-client-mock:1.6.2") // Ktor - http mock
    api("io.ktor:ktor-server-test-host:1.6.2") // Ktor - test engine
    api("io.kotest:kotest-runner-junit5:4.6.1") // Kotest - test framework
    api("io.kotest:kotest-property:4.6.1") // Kotest - property testing
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")

open class DependentsTask : DefaultTask() {

    @Input
    val libName: String = "hops-common-test"

    @TaskAction
    fun dependents() {
        project.parent?.parent?.subprojects?.forEach { subProj ->
            subProj.subprojects.forEach { subSubProj ->
                when (subSubProj.name) {
                    libName -> logger.debug("ignore self")
                    else -> {
                        val dependents = subSubProj.configurations
                            .testRuntimeClasspath.get()
                            .resolvedConfiguration
                            .files
//                            .resolvedArtifacts
//                            .stream()
//                            .map { it.id.componentIdentifier }
//                            .map { it as? ProjectComponentIdentifier }
//                            .filter { it != null }
//                            .toList()
                        logger.warn("dependents: $dependents")
                    }
                }
            }
        } ?: logger.error("something was null")
    }
}
