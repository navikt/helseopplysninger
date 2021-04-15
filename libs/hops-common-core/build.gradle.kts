import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

val ktorVersion = "1.5.3"
val naviktTokenSupportVersion = "1.3.3"
val junitVersion = "5.7.1"
val mockOauthVersion = "0.3.1"
val logbackVersion = "1.2.3"
val kotlinxVersion = "1.4.3"
val kotestVersion = "4.4.3"

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
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinxVersion")
    implementation("no.nav.security:token-client-core:$naviktTokenSupportVersion")
    implementation("io.ktor:ktor-client-jackson:$ktorVersion")
    implementation("io.ktor:ktor-client-cio-jvm:$ktorVersion")
    testImplementation("io.ktor:ktor-server-test-host:$ktorVersion") { exclude(group = "junit", module = "junit") }
    testImplementation("no.nav.security:mock-oauth2-server:$mockOauthVersion")
    testImplementation("org.junit.jupiter:junit-jupiter:$junitVersion")
    testImplementation("io.kotest:kotest-assertions-core-jvm:$kotestVersion")
    testImplementation("io.ktor:ktor-server-netty:$ktorVersion")
    testImplementation("io.ktor:ktor-jackson:$ktorVersion")
    testImplementation("no.nav.security:token-validation-ktor:$naviktTokenSupportVersion")
    testRuntimeOnly("io.kotest:kotest-runner-junit5-jvm:$kotestVersion")
    testRuntimeOnly("ch.qos.logback:logback-classic:$logbackVersion")
}
