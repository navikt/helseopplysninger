import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.5.1"
val logback_version = "1.2.3"
val koin_version = "2.2.2"
val hapi_version = "5.2.1"
val token_validation_version = "1.3.3"
val mock_oauth_version = "0.3.1"
val junit_version = "5.7.1"

plugins {
    java
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
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks.withType<KotlinCompile>().all {
    kotlinOptions.jvmTarget = "11"
}

tasks.test {
    useJUnitPlatform()
}

dependencies {
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-auth-jwt:$ktor_version")
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("io.ktor:ktor-client-cio:$ktor_version")
    implementation("io.ktor:ktor-client-auth:$ktor_version")
    implementation("io.ktor:ktor-client-jackson:$ktor_version")
    implementation("no.nav.security:token-validation-ktor:$token_validation_version")
    implementation("org.koin:koin-ktor:$koin_version")
    implementation("org.koin:koin-logger-slf4j:$koin_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapi_version")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version") { exclude(group = "junit", module = "junit") }
    testImplementation("io.ktor:ktor-client-mock:$ktor_version")
    testImplementation("org.koin:koin-test:$koin_version") { exclude(group = "junit", module = "junit") }
    testImplementation("org.junit.jupiter:junit-jupiter:$junit_version")
    testImplementation("no.nav.security:mock-oauth2-server:$mock_oauth_version")
}