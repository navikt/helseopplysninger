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
    api(project(":libs:hops-common-ktor"))
    implementation("ca.uhn.hapi.fhir:hapi-fhir-structures-r4:$hapiVersion")
    implementation("com.sksamuel.hoplite:hoplite-core:1.4.0")
    implementation("io.insert-koin:koin-ktor:3.0.1")
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("org.apache.kafka:kafka-clients:2.8.0")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    runtimeOnly("ca.uhn.hapi.fhir:hapi-fhir-client:$hapiVersion")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.3")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
}
