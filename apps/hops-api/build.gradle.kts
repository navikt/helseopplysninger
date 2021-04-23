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
    val hapiVersion = "5.3.2"

    api(project(":libs:hops-common-fhir"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:1.4.0")
    implementation("no.nav.security:token-validation-ktor:1.3.5")
    implementation("io.insert-koin:koin-ktor:3.0.1")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:1.6.6")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("no.nav.security:mock-oauth2-server:0.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
}
