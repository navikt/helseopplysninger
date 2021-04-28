import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

project.setProperty("mainClassName", "io.ktor.server.netty.EngineMain") // Required by shadowJar

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
    val ktorVersion = "1.5.3"
    val hapiVersion = "5.3.3"
    val junitVersion = "5.7.1"

    api(project(":libs:hops-common-fhir"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:1.4.0")
    implementation("no.nav.security:token-validation-ktor:1.3.5")
    implementation("io.insert-koin:koin-ktor:3.0.1")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.6.6")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("io.ktor:ktor-client-cio:$ktorVersion")
    runtimeOnly("io.ktor:ktor-server-netty:$ktorVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    testImplementation(kotlin("test-junit5"))
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude(group = "junit", module = "junit") }
    testImplementation("no.nav.security:mock-oauth2-server:0.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitVersion")
    testImplementation("org.junit.jupiter:junit-jupiter-params:$junitVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitVersion")
}
