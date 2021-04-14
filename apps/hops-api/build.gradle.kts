import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktor_version = "1.5.3"
val logback_version = "1.2.3"
val logstash = "6.6"
val mock_oauth = "0.3.1"
val token_support = "1.3.4"
val junit = "5.7.1"
val prometeus_version = "1.6.5"

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow") version "5.0.0"
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
    // mainClass.set("io.ktor.server.netty.EngineMain") funker ikke med shadowJar atm
}

repositories {
    mavenCentral()
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
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
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:$ktor_version")
    implementation("ch.qos.logback:logback-classic:$logback_version")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstash")
    implementation("io.ktor:ktor-auth:$ktor_version")
    implementation("io.ktor:ktor-metrics-micrometer:$ktor_version")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeus_version")
    implementation("no.nav.security:token-validation-ktor:${token_support}")
    testImplementation("io.ktor:ktor-server-test-host:$ktor_version")
    testImplementation("no.nav.security:mock-oauth2-server:$mock_oauth")
    testImplementation("org.junit.jupiter:junit-jupiter:${junit}")
}