plugins {
    application
    id("org.springframework.boot") version "2.5.1"
    id("io.spring.dependency-management") version "1.0.11.RELEASE"
}

java {
    sourceCompatibility = JavaVersion.VERSION_11
}

tasks {
    test {
        useJUnitPlatform()
    }
}

dependencies {
    val hapiVersion = "5.3.3"
    val springBootVersion = "2.4.5"
    val h2Version = "1.4.200"

    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-cql:$hapiVersion")
    implementation("ca.uhn.hapi.fhir:hapi-fhir-jpaserver-mdm:$hapiVersion")
    implementation("org.springframework.boot:spring-boot-autoconfigure:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server:$springBootVersion")
    runtimeOnly("io.micrometer:micrometer-registry-prometheus:1.7.0")
    runtimeOnly("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-data-jpa:$springBootVersion")
    runtimeOnly("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    runtimeOnly("org.postgresql:postgresql:42.2.22")
    runtimeOnly("net.logstash.logback:logstash-logback-encoder:6.6")
    testImplementation("org.awaitility:awaitility:4.1.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
    testRuntimeOnly("com.h2database:h2:$h2Version")
    developmentOnly("com.h2database:h2:$h2Version")
    developmentOnly("org.springframework.boot:spring-boot-devtools:$springBootVersion")
}
