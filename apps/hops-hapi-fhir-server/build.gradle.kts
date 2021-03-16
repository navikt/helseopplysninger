object Version {
    const val hapi = "5.3.0"
    const val spring_boot = "2.4.3"
    const val postgresql = "42.2.19"
    const val logstash = "6.6"
    const val h2 = "1.4.200"
    const val micrometer_prometheus = "1.6.4"
}

plugins {
    application
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

repositories {
    mavenCentral()
    maven("https://github-package-registry-mirror.gc.nav.no/cached/maven-release")
}

dependencies {
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-cql:${Version.hapi}")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-mdm:${Version.hapi}")
    implementation("org.springframework.boot:spring-boot-autoconfigure:${Version.spring_boot}")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:${Version.spring_boot}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator:${Version.spring_boot}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa:${Version.spring_boot}")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web:${Version.spring_boot}")
    runtimeOnly("org.postgresql:postgresql:${Version.postgresql}")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:${Version.logstash}")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:${Version.micrometer_prometheus}")
    developmentOnly("com.h2database:h2:${Version.h2}")
    developmentOnly("org.springframework.boot:spring-boot-devtools:${Version.spring_boot}")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}