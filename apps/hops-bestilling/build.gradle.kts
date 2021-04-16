import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.5.3"
val koinVersion = "3.0.1-beta-2"
val hapiVersion = "5.2.1"
val tokenValidationVersion = "1.3.3"
val junitVersion = "5.7.1"
val mockOauthVersion = "0.3.1"
val logbackVersion = "1.2.3"
val logstashVersion = "6.6"
val kafkaVersion = "2.7.0"
val hopliteVersion = "1.4.0"

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
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
    api(project(":libs:hops-common-ktor"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("com.sksamuel.hoplite:hoplite-core:$hopliteVersion")
    implementation("io.insert-koin:koin-ktor:$koinVersion")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.apache.kafka:kafka-clients:$kafkaVersion")
    testImplementation("io.insert-koin:koin-test:$koinVersion") { exclude(group = "junit", module = "junit") }
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    runtimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
}
