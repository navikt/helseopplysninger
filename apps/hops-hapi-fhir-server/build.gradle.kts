val hapiVersion = "5.3.0"
val springBootVersion = "2.4.3"
val postgresqlVersion = "42.2.19"
val logstashVersion = "6.6"
val h2Version = "1.4.200"
val micrometerPrometheusVersion = "1.6.4"

plugins {
    application
    id("org.springframework.boot") version "2.4.3"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

dependencies {
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-cql:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-mdm:$hapiVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    runtimeOnly("org.postgresql:postgresql:$postgresqlVersion")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:$logstashVersion")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:$micrometerPrometheusVersion")
    developmentOnly("com.h2database:h2:$h2Version")
    developmentOnly("org.springframework.boot:spring-boot-devtools:$springBootVersion")
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}