import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.5.0"
val logbackVersion = "1.2.3"
val logstashVersion = "6.6"
val mockOauthVersion = "0.3.1"
val tokenSupportVersion = "1.3.5"
val junitVersion = "5.7.1"
val prometeusVersion = "1.6.5"

plugins {
    application
    kotlin("jvm")
    id("com.github.johnrengelman.shadow")
}

application {
    mainClassName = "io.ktor.server.netty.EngineMain"
    // mainClass.set("io.ktor.server.netty.EngineMain") funker ikke med shadowJar atm
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
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
    implementation(kotlin("stdlib-jdk8"))
    implementation("io.ktor:ktor-server-netty:$ktorVersion")
    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    implementation("io.ktor:ktor-auth:$ktorVersion")
    implementation("io.ktor:ktor-metrics-micrometer:$ktorVersion")
    implementation("io.micrometer:micrometer-registry-prometheus:$prometeusVersion")
    implementation("no.nav.security:token-validation-ktor:$tokenSupportVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion")
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
}