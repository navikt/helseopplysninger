val hapi_version = "5.3.0"
val spring_boot_version = "2.4.3"
val postgresql_version = "42.2.19"
val logstash_version = "6.6"
val h2_version = "1.4.200"
val micrometer_prometheus_version = "1.6.4"

plugins {
    application
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

repositories {
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
    mavenCentral()
}

dependencies {
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-cql:${hapi_version}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-mdm:${hapi_version}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${spring_boot_version}")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:${spring_boot_version}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator:${spring_boot_version}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa:${spring_boot_version}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web:${spring_boot_version}")
    runtimeOnly("org.postgresql:postgresql:${postgresql_version}")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${logstash_version}")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:${micrometer_prometheus_version}")
    developmentOnly("com.h2database:h2:${h2_version}")
    developmentOnly("org.springframework.boot:spring-boot-devtools:${spring_boot_version}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}