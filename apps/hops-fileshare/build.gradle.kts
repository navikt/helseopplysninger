import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("fileshare.AppKt")
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
    implementation("io.ktor:ktor-metrics-micrometer:1.6.4")
    implementation("io.ktor:ktor-webjars:1.6.4")
    implementation("io.ktor:ktor-server-netty:1.6.4")
    implementation("io.ktor:ktor-auth:1.6.4")
    implementation("io.ktor:ktor-client-auth:1.6.4")
    implementation("io.ktor:ktor-client-serialization:1.6.4")
    implementation("no.nav.security:token-validation-ktor:1.3.9")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.4")
    implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.3.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.6")
    runtimeOnly("io.ktor:ktor-server-netty:1.6.3")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.webjars:swagger-ui:3.52.5")
    testImplementation(project(":libs:hops-common-test"))
    testImplementation("io.kotest:kotest-property:4.6.3") // Kotest - property testing
    testImplementation("io.kotest:kotest-assertions-ktor:4.4.3") // Kotest - ktor matchers
    testImplementation("io.kotest:kotest-assertions-shared:4.6.3")
    testRuntimeOnly("io.kotest:kotest-runner-junit5:4.6.3") // Kotest - test framework
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
