import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.5.1"
val logback_version = "1.2.3"
val koin_version = "2.2.2"
val hapi_version = "5.2.1"

plugins {
    application
    kotlin("jvm") version "1.4.21"
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
    // mainClass.set("io.ktor.server.netty.EngineMain") funker ikke med shadowJar atm
}

repositories {
    mavenCentral()
    jcenter()
    maven ("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

java {
    sourceCompatibility = JavaVersion.VERSION_1_8
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "1.8"
}

dependencies {
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("org.koin:koin-ktor:$koin_version")
    implementation("org.koin:koin-logger-slf4j:$koin_version")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapi_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("org.koin:koin-test:$koin_version")
}