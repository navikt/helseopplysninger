import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain") // Required by shadowJar
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
    val junitVersion = "5.7.2"
    val ktorVersion = "1.6.0"

    api(project(":libs:hops-common-fhir"))
    api(project(":libs:hops-common-ktor"))
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.1")
    implementation("no.nav.security:token-validation-ktor:1.3.8")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("io.ktor:ktor-server-netty:$ktorVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude("org.jetbrains.kotlin", "kotlin-test-junit") }
    testImplementation("no.nav.security:mock-oauth2-server:0.3.4")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testImplementation(kotlin("test-junit5"))
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
