import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("eventsink.AppKt") // Required by shadowJar
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
    implementation(project(":libs:hops-common-kafka"))
    implementation("io.ktor:ktor-metrics-micrometer:1.6.5")
    implementation("io.ktor:ktor-webjars:1.6.6")
    implementation("io.ktor:ktor-server-netty:1.6.5")
    implementation("io.micrometer:micrometer-registry-prometheus:1.8.0")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.7")
    runtimeOnly("io.ktor:ktor-server-netty:1.6.5")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:7.0.1")
    runtimeOnly("org.webjars:swagger-ui:4.1.0")
    testImplementation(project(":libs:hops-common-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
