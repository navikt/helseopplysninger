plugins {
    application
    kotlin("jvm")
    kotlin("plugin.serialization") version "1.5.30"
    id("com.github.johnrengelman.shadow")
    id("org.jlleitschuh.gradle.ktlint")
}

application {
    mainClass.set("e2e.AppKt")
}

dependencies {
    implementation(project(":libs:hops-common-ktor"))
    implementation("io.micrometer:micrometer-registry-prometheus:1.7.4")
    implementation("io.ktor:ktor-metrics-micrometer:1.6.3")
    implementation("io.ktor:ktor-client-cio:1.6.3")
    implementation("io.ktor:ktor-server-netty:1.6.3")
    implementation("io.ktor:ktor-client-serialization:1.6.3")
    runtimeOnly("io.ktor:ktor-server-netty:1.6.3")
    runtimeOnly("ch.qos.logback:logback-classic:1.2.5")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    implementation("io.ktor:ktor-serialization:1.6.3")
    testImplementation(project(":libs:hops-common-test"))
}

tasks {
    withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
        kotlinOptions.jvmTarget = "16"
        kotlinOptions.freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
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

kotlin.sourceSets["main"].kotlin.srcDirs("main")
kotlin.sourceSets["test"].kotlin.srcDirs("test")
sourceSets["main"].resources.srcDir(".config")
