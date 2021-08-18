import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
    id("io.kotest")
}

application {
    mainClass.set("io.ktor.server.netty.EngineMain") // Required by shadowJar
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
    implementation("io.ktor:ktor-auth:1.6.2")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.2")
    implementation("io.ktor:ktor-server-netty:1.6.2")
    implementation("io.ktor:ktor-webjars:1.6.2")
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.3")
    implementation("no.nav.security:token-validation-ktor:1.3.8")
    implementation(project(":libs:hops-common-ktor"))

    runtimeOnly("ch.qos.logback:logback-classic:1.2.5")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    runtimeOnly("org.webjars:swagger-ui:3.51.2")

    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
