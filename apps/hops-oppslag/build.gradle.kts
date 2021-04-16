import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.5.2"
val koinVersion = "3.0.1-beta-2"
val hapiVersion = "5.2.1"
val tokenValidationVersion = "1.3.3"
val junitVersion = "5.7.1"
val mockOauthVersion = "0.3.1"
val logbackVersion = "1.2.3"
val logstashVersion = "6.6"
val micrometerPrometheusVersion = "1.6.6"
val hopliteVersion = "1.4.0"

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
    // mainClass.set("io.ktor.server.netty.EngineMain") funker ikke med shadowJar atm
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
    api(project(":libs:hops-common-fhir"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("com.sksamuel.hoplite:hoplite-hocon:$hopliteVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("no.nav.security:token-validation-ktor:$tokenValidationVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$micrometerPrometheusVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude(group = "junit", module = "junit") }
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("io.ktor:ktor-client-cio:$ktorVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
}