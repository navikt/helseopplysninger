import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain")
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
    implementation(project(":libs:hops-common-ktor"))
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.12.5")
    implementation("io.github.microutils:kotlin-logging-jvm:2.0.10")
    implementation("com.nimbusds:nimbus-jose-jwt:9.13")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("io.ktor:ktor-webjars:1.6.3")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.6")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.webjars:swagger-ui:3.52.1")
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
