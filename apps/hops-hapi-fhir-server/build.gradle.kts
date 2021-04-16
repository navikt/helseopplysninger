plugins {
    application
    id("org.springframework.boot") version "2.4.5"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

dependencies {
    val hapiVersion = "5.3.2"
    val springBootVersion = "2.4.4"

    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-cql:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-mdm:$hapiVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:$springBootVersion")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:1.6.6")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    runtimeOnly("org.postgresql:postgresql:42.2.19")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    developmentOnly("com.h2database:h2:1.4.200")
    developmentOnly("org.springframework.boot:spring-boot-devtools:$springBootVersion")
}