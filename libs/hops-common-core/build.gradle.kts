import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    `java-library`
    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
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
    val ktorVersion = "1.5.2"
    val naviktTokenSupportVersion = "1.3.3"
    val kotestVersion = "4.4.3"

    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:1.4.3-native-mt")
    implementation("no.nav.security:token-client-core:$naviktTokenSupportVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    testImplementation("no.nav.security:mock-oauth2-server:0.3.2")
    testImplementation("org.junit.jupiter:junit-jupiter:5.7.1")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude(group = "junit", module = "junit") }
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-jackson:$ktorVersion")
    testImplementation("no.nav.security:token-validation-ktor:$naviktTokenSupportVersion")
    testRuntimeOnly("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:1.2.3")
}
